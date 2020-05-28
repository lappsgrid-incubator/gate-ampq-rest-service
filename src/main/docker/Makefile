NAME=ampq-rest-service
JAR=$(NAME).jar
TARGET=../../../target/$(JAR)
REPO=docker.lappsgrid.org
GROUP=gate
IMAGE=$(REPO)/$(GROUP)/$(NAME)

docker:
	if [[ $(TARGET) -nt $(JAR) ]] ; then cp $(TARGET) . ; fi
	docker build -t $(IMAGE) .

start:
	docker run -d -p "8080:8080" --name $(NAME) $(IMAGE)

stop:
	docker rm -f $(NAME)

run:
	docker run -it --rm $(IMAGE) /bin/bash

