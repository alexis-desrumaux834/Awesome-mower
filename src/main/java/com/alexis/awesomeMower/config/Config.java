package com.alexis.awesomeMower.config;

import java.util.List;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.transaction.PlatformTransactionManager;

import com.alexis.awesomeMower.entities.LawnMower;
import com.alexis.awesomeMower.entities.MowingConfig;
import com.alexis.awesomeMower.src.MowingProcessor;
import com.alexis.awesomeMower.src.ConfigFileReader;
import com.alexis.awesomeMower.src.ResultFileWriter;

@Configuration
public class Config {
    private final JobRepository jobRepository;
    private Resource resource;
    private PlatformTransactionManager transactionManager;

    public Config(MowingProcessor processor,
            JobRepository jobRepository,
            @Value("classpath:input.txt") Resource resource,
            PlatformTransactionManager transactionManager) {
        this.resource = resource;
        this.transactionManager = transactionManager;
        this.jobRepository = jobRepository;
    }

    @Bean
    public Job createJob(Step lawnMowerStep) {
        return new JobBuilder("AwesomeMowerJob", jobRepository)
                .start(lawnMowerStep)
                .build();
    }

    @Bean
    public Step createStep() {
        return new StepBuilder("AwesomeMowerStep", jobRepository)
                .<MowingConfig, List<LawnMower>>chunk(1, transactionManager)
                .reader(linkReader())
                .processor(linkProcessor())
                .writer(new ResultFileWriter(new FileSystemResource("target/output.txt")))
                .build();
    }

    @Bean
    public ConfigFileReader linkReader() {
        ConfigFileReader configFileReader = new ConfigFileReader(this.resource);
        return configFileReader;
    }

    @Bean()
    public ItemProcessor<MowingConfig, List<LawnMower>> linkProcessor() {
        return new MowingProcessor();
    }
}