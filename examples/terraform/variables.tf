variable "region" {
  description = "AWS region"
  type        = string
  default     = "us-east-1"
}

variable "app_name" {
  description = "Application name"
  type        = string
  default     = "SpringAiAgentJava"
}

variable "image_tag" {
  description = "Container image tag"
  type        = string
  default     = "latest"
}
