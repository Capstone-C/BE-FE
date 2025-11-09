variable "db_password" {
  description = "RDS database password"
  type        = string
  sensitive   = true # (중요) plan/apply 시 터미널에 비밀번호가 안 보이게 함
}

variable "ecr_image_url" {
  description = "ECR에 푸시한 백엔드 앱 이미지 URL"
  type        = string
}

variable "my_ip" {
  description = "내 노트북 현재 ip"
  type        = string
}

