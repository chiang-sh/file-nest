package io.github.chiang_sh.file_nest.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaConfig {
    @Bean
    public NewTopic fileDeleteTopic() {
        return TopicBuilder.name("file-delete").partitions(1).replicas(1).build();
    }
}
