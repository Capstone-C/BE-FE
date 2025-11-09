terraform {
  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 6.0"
    }
  }
}

# Configure the AWS Provider
provider "aws" {
  region = "ap-northeast-2"
}

# Create a VPC
resource "aws_vpc" "prod" {
  cidr_block = "10.0.0.0/16"
  tags = {
    Name = "prod_vpc"
  }
}

resource "aws_internet_gateway" "gw" {
  vpc_id = aws_vpc.prod.id

  tags = {
    Name = "prod_igw"
  }
}

#################
### routtable ###
#################
resource "aws_route_table" "public" {
  vpc_id = aws_vpc.prod.id

  route {
    cidr_block = "0.0.0.0/0"
    gateway_id = aws_internet_gateway.gw.id
  }

  tags = {
    Name = "prod_public_rt"
  }
}

resource "aws_route_table_association" "pub_a" {
  subnet_id      = aws_subnet.prod_pub_a.id
  route_table_id = aws_route_table.public.id
}

resource "aws_route_table_association" "pub_c" {
  subnet_id      = aws_subnet.prod_pub_c.id
  route_table_id = aws_route_table.public.id
}

##############
### subnet ###
##############

resource "aws_subnet" "prod_pub_a" {
  vpc_id            = aws_vpc.prod.id
  cidr_block        = "10.0.10.0/24"
  availability_zone = "ap-northeast-2a"

  tags = {
    Name = "prod_pub_a"
  }
}
resource "aws_subnet" "prod_pub_c" {
  vpc_id            = aws_vpc.prod.id
  cidr_block        = "10.0.20.0/24"
  availability_zone = "ap-northeast-2c"

  tags = {
    Name = "prod_pub_c"
  }
}

resource "aws_subnet" "prod_was_a" {
  vpc_id            = aws_vpc.prod.id
  cidr_block        = "10.0.11.0/24"
  availability_zone = "ap-northeast-2a"

  tags = {
    Name = "prod_was_a"
  }
}

resource "aws_subnet" "prod_db_a" {
  vpc_id            = aws_vpc.prod.id
  cidr_block        = "10.0.12.0/24"
  availability_zone = "ap-northeast-2a"

  tags = {
    Name = "prod_db_a"
  }
}

resource "aws_subnet" "prod_was_c" {
  vpc_id            = aws_vpc.prod.id
  cidr_block        = "10.0.21.0/24"
  availability_zone = "ap-northeast-2c"

  tags = {
    Name = "prod_was_c"
  }
}


resource "aws_subnet" "prod_db_c" {
  vpc_id            = aws_vpc.prod.id
  cidr_block        = "10.0.22.0/24"
  availability_zone = "ap-northeast-2c"

  tags = {
    Name = "prod_db_c"
  }
}

####################################
###   베스천 호스트 EC2 인스턴스  ###
####################################
resource "aws_security_group" "prod_bastion_sg" {
  name   = "prod-bastion-sg"
  vpc_id = aws_vpc.prod.id
  description = "Security group for Bastion Host"

  # 인바운드: "내 IP"(관리자)에서만 SSH(22) 허용
  ingress {
    from_port   = 22
    to_port     = 22
    protocol    = "tcp"
    cidr_blocks = [var.my_ip] 
    description = "Allow SSH from My IP"
  }
  
  # 아웃바운드: 모든 트래픽 허용
  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
    description = "Allow all outbound traffic"
  }

  tags = {
    Name = "prod-bastion-sg"
  }
}


# 퍼블릭 서브넷에 베스천 EC2 인스턴스를 생성
resource "aws_instance" "prod_bastion_host" {
  # Amazon Linux 2 AMI (Launch Template과 동일)
  ami           = "ami-077ad873396d76f6a" 
  instance_type = "t2.micro"
  key_name      = "prod_key" # WAS와 동일한 키 페어 사용

  # 1. 퍼블릭 서브넷에 배치 (ALB와 동일)
  subnet_id = aws_subnet.prod_pub_a.id

  # 2. 위에서 만든 베스천 전용 보안 그룹 할당
  vpc_security_group_ids = [aws_security_group.prod_bastion_sg.id]

  # 3. 퍼블릭 서브넷이므로 공인 IP 자동 할당
  associate_public_ip_address = true

  tags = {
    Name = "prod-bastion-host"
  }
}

########################
### ALB, TargetGroup ### 
########################

resource "aws_launch_template" "prod_was_lt" {
  name_prefix   = "prod_was_lt"
  image_id      = "ami-077ad873396d76f6a"
  instance_type = "t2.micro"
  key_name      = "prod_key"

  # EIP 자동 할당
  network_interfaces {
    associate_public_ip_address = false
    security_groups             = [aws_security_group.prod_was_sg.id]
  }

  tags = {
    Name = "prod_was_lt"
  }

  lifecycle {
    create_before_destroy = true
  }

  user_data = base64encode(<<-EOF
    #!/bin/bash
    # 1. 시스템 업데이트 및 Docker 설치
    yum update -y
    amazon-linux-extras install docker -y
    service docker start
    usermod -a -G docker ec2-user # ec2-user가 sudo 없이 docker 명령어 쓰도록

    # 2. Docker Compose 설치 (사용자님이 요청하신 부분)
    curl -L "https://github.com/docker/compose/releases/latest/download/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
    chmod +x /usr/local/bin/docker-compose

    # 3. ECR 로그인 - 리전은 Terraform provider와 동일하게 ap-northeast-2로 가정
    # ECR URI의 계정 ID 부분을 추출 (123456789012.dkr.ecr...)
    ECR_ACCOUNT_ID=$(echo "${var.ecr_image_url}" | cut -d. -f1)
    aws ecr get-login-password --region ap-northeast-2 | docker login --username AWS --password-stdin $${ECR_ACCOUNT_ID}.dkr.ecr.ap-northeast-2.amazonaws.com

    # 4. docker-compose.yml 파일 생성
    # Terraform 변수(RDS 엔드포인트, DB 비밀번호 등)를 사용해 동적으로 파일을 생성
    cat <<EOT > /home/ec2-user/docker-compose.yml
    version: '3.7'
    services:
      app:
        image: "${var.ecr_image_url}" # Terraform 변수로 ECR 이미지 주소 전달
        ports:
          # Target Group 포트(80)와 컨테이너 내부 포트(Java Spring: 8080)를 매핑
          # Dockerfile에서 8080을 쓴다고 가정
          - "80:8080" 
        environment:
          # Spring Boot가 RDS를 바라보도록 환경변수 주입
          # Terraform이 생성한 RDS의 엔드포인트 주소를 직접 주입
          SPRING_DATASOURCE_URL: "jdbc:mysql://${aws_db_instance.prod_db.endpoint}/ccdb?useSSL=false&serverTimezone=UTC&LegacyDateTimeCode=false"
          SPRING_DATASOURCE_USERNAME: "${aws_db_instance.prod_db.username}" # Terraform DB 리소스의 username
          SPRING_DATASOURCE_PASSWORD: "${var.db_password}" # Terraform 변수로 비밀번호 전달
          TESSDATA_PREFIX: "/usr/share/tesseract-ocr/5/tessdata" # Dockerfile의 ENV와 동일하게
        restart: always
    EOT
    
    # 생성된 파일의 소유자를 ec2-user로 변경
    chown ec2-user:ec2-user /home/ec2-user/docker-compose.yml

    # 5. Docker Compose 실행
    cd /home/ec2-user
    docker-compose up -d
    EOF
  )
}

resource "aws_autoscaling_group" "prod_was_asg" {
  name                = "prod-was-asg"
  desired_capacity    = 2
  max_size            = 4
  min_size            = 2
  vpc_zone_identifier = [aws_subnet.prod_was_a.id, aws_subnet.prod_was_c.id]

  launch_template {
    id      = aws_launch_template.prod_was_lt.id
    version = "$Latest"
  }

  target_group_arns = [aws_lb_target_group.prod_was_tg.arn]

  health_check_type         = "ELB"
  health_check_grace_period = 300

  tag {
    key                 = "Name"
    value               = "prod-was-instance"
    propagate_at_launch = true #ASG가 생성하는 EC2에 tag 붙히기
  }
}



#######################
### Security Groups ###
#######################
resource "aws_security_group" "prod_was_sg" {
  name        = "prod-was-sg"
  description = "Security group for WAS servers"
  vpc_id      = aws_vpc.prod.id

  # 인바운드 규칙: ALB에서 오는 HTTP 허용
  ingress {
    from_port       = 80
    to_port         = 80
    protocol        = "tcp"
    security_groups = [aws_security_group.prod_alb_sg.id]
    description     = "Allow HTTP from ALB"
  }

  ingress {
    from_port   = 22
    to_port     = 22
    protocol    = "tcp"
    security_groups = [aws_security_group.prod_bastion_sg.id]
    description = "Allow SSH from My IP"
  }



  # 아웃바운드 규칙: 모든 트래픽 허용
  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
    description = "Allow all outbound traffic"
  }

  tags = {
    Name = "prod-was-sg"
  }
}

resource "aws_security_group" "prod_alb_sg" {
  name        = "prod-alb-sg"
  description = "Security group for public ALB, locked to CloudFront"
  vpc_id      = aws_vpc.prod.id

  # 인바운드 규칙: 외부 HTTP/HTTPS 허용
  ingress {
    from_port   = 80
    to_port     = 80
    protocol    = "tcp"
    prefix_list_ids = ["pl-22a6434b"]
    description = "Allow HTTP ONLY from CloudFront"
  }

  ingress {
    from_port   = 443
    to_port     = 443
    protocol    = "tcp"
    prefix_list_ids = ["pl-22a6434b"]
    description = "Allow HTTPS ONLY from CloudFront"
  }

  # 아웃바운드 규칙: 모든 트래픽 허용
  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
    description = "Allow all outbound traffic"
  }

  tags = {
    Name = "prod-alb-sg"
  }
}

########################
### ALB, TargetGroup ### 
########################

resource "aws_lb" "prod_lb" {
  name               = "prod-lb"
  internal           = false
  load_balancer_type = "application"
  security_groups    = [aws_security_group.prod_alb_sg.id]
  subnets            = [aws_subnet.prod_pub_a.id, aws_subnet.prod_pub_c.id]


  tags = {
    Environment = "prod"
  }
}


resource "aws_lb_listener" "prod_listener" {
  load_balancer_arn = aws_lb.prod_lb.arn
  port              = "80"
  protocol          = "HTTP"

  default_action {
    type             = "forward"
    target_group_arn = aws_lb_target_group.prod_was_tg.arn
  }
}

resource "aws_lb_target_group" "prod_was_tg" {
  name     = "prod-was-tg"
  port     = 80
  protocol = "HTTP"
  vpc_id   = aws_vpc.prod.id
  health_check {
    path                = "/"
    protocol            = "HTTP"
    interval            = 30
    timeout             = 5
    healthy_threshold   = 2
    unhealthy_threshold = 2
  }

  tags = {
    Name = "prod_was_tg"
  }
}

########################
###        RDS       ### 
########################

resource "aws_db_subnet_group" "prod_db_sg" {
  name       = "prod-db-subnect-group"
  subnet_ids = [aws_subnet.prod_db_a.id, aws_subnet.prod_db_c.id]
  tags = {
    Name = "Prod DB Subnet Group"
  }
}

resource "aws_security_group" "prod_db_sg" {
  name        = "prod-db-sg"
  description = "Security group for RDS DB"
  vpc_id      = aws_vpc.prod.id

  # 인바운드 규칙: WAS SG에서 오는 MySQL(3306) 트래픽만 허용
  ingress {
    from_port       = 3306
    to_port         = 3306
    protocol        = "tcp"
    security_groups = [aws_security_group.prod_was_sg.id] # WAS SG ID 참조
    description     = "Allow MySQL from WAS SG"
  }

  # 아웃바운드 규칙 (기본값: 모두 허용)
  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = {
    Name = "prod-db-sg"
  }
}

resource "aws_db_instance" "prod_db" {
  identifier        = "prod-db-instance"
  allocated_storage = 20
  engine            = "mysql"
  engine_version    = "8.0"
  instance_class    = "db.t3.micro"
  multi_az          = true


  db_name  = "ccdb"
  username = "ccuser"
  password = var.db_password

  db_subnet_group_name   = aws_db_subnet_group.prod_db_sg.name
  vpc_security_group_ids = [aws_security_group.prod_db_sg.id]
  skip_final_snapshot    = true

  tags = {
    Name = "prod_db"
  }
}