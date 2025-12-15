variable "aws_region" {
  description = "AWS region for resources"
  type        = string
  default     = "us-east-1"
}

variable "memory_name" {
  description = "Base name for the AgentCore Memory (will be made unique)"
  type        = string
  default     = "springAiExampleMemory"
  
  validation {
    condition     = can(regex("^[a-zA-Z][a-zA-Z0-9_-]*$", var.memory_name))
    error_message = "Memory name must start with a letter and contain only letters, numbers, underscores, and hyphens."
  }
}

variable "environment" {
  description = "Environment name"
  type        = string
  default     = "development"
}
