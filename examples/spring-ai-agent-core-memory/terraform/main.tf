terraform {
  required_version = ">= 1.0"
  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 5.0"
    }
    random = {
      source  = "hashicorp/random"
      version = "~> 3.1"
    }
  }
}

provider "aws" {
  region = var.aws_region
}

# Generate a unique memory name that follows AWS naming constraints
resource "random_string" "suffix" {
  length  = 8
  special = false
  upper   = false
}

locals {
  # AWS requires pattern: [a-zA-Z][a-zA-Z0-9_]{0,47}
  memory_name_clean = replace(var.memory_name, "-", "_")
  unique_memory_name = "${local.memory_name_clean}_${random_string.suffix.result}"
}

# Create AgentCore Memory using AWS CLI
resource "null_resource" "agentcore_memory" {
  provisioner "local-exec" {
    command = <<-EOT
      aws bedrock-agentcore-control create-memory \
        --name "${local.unique_memory_name}" \
        --description "Memory for Spring AI AgentCore example - ${var.environment}" \
        --event-expiry-duration 365 \
        --region "${var.aws_region}" \
        --output json > memory_output.json
      
      MEMORY_ID=$(cat memory_output.json | jq -r '.memory.id')
      echo "Waiting for memory $MEMORY_ID to become ACTIVE..."
      
      for i in {1..60}; do
        STATUS=$(aws bedrock-agentcore-control get-memory \
          --memory-id "$MEMORY_ID" \
          --region "${var.aws_region}" \
          --query 'memory.status' \
          --output text)
        
        if [ "$STATUS" = "ACTIVE" ]; then
          echo "Memory is ACTIVE"
          break
        fi
        
        echo "Status: $STATUS, waiting... ($i/60)"
        sleep 5
      done
    EOT
  }

  provisioner "local-exec" {
    when    = destroy
    command = <<-EOT
      if [ -f memory_output.json ]; then
        MEMORY_ID=$(cat memory_output.json | jq -r '.memory.id // empty')
        if [ ! -z "$MEMORY_ID" ]; then
          echo "Attempting to delete memory: $MEMORY_ID"
          aws bedrock-agentcore-control delete-memory \
            --memory-id "$MEMORY_ID" \
            --region "${self.triggers.aws_region}" || echo "Failed to delete memory (may already be deleted)"
        fi
        rm -f memory_output.json
      fi
    EOT
  }

  triggers = {
    memory_name = local.unique_memory_name
    aws_region  = var.aws_region
    environment = var.environment
  }
}

# Extract memory ID from the created memory
data "local_file" "memory_output" {
  filename   = "${path.module}/memory_output.json"
  depends_on = [null_resource.agentcore_memory]
}

locals {
  memory_data = jsondecode(data.local_file.memory_output.content)
  memory_id   = local.memory_data.memory.id
  memory_arn  = local.memory_data.memory.arn
  memory_name = local.memory_data.memory.name
}

# Output the memory ID for use in Spring application
output "memory_id" {
  description = "The ID of the created AgentCore Memory"
  value       = local.memory_id
}

output "memory_arn" {
  description = "The ARN of the created AgentCore Memory"
  value       = local.memory_arn
}

output "memory_name" {
  description = "The name of the created AgentCore Memory"
  value       = local.memory_name
}

output "export_command" {
  description = "Command to export memory ID as environment variable"
  value       = "export AGENTCORE_MEMORY_ID=${local.memory_id}"
}
