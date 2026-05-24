variable "prefix" {
  description = "Prefix for all resources"
  default     = "dev"
}

variable "region" {
  description = "region"
  default     = "ap-northeast-2"
}

variable "nickname" {
  description = "nickname"
  default     = "jhs512"
}

variable "ami" {
  description = "EC2 AMI ID (Ubuntu 22.04 LTS in ap-northeast-2)"
  default     = "ami-0c9c942bd7bf113a2"
}

variable "instance_type" {
  description = "EC2 instance type"
  default     = "t3.small"
}

variable "public_key" {
  description = "SSH public key contents"
}