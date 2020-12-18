FROM flink:scala_2.11-java8

ARG INPUT_PATH

WORKDIR /

RUN mkdir /inputs &&\
    mkdir /outputs && \
    chmod -R 777 /outputs/ &&\
    chmod -R 777 /inputs/

ADD ${INPUT_PATH} /inputs