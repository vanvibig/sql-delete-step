package com.kv.async.step.demo;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.item.database.builder.JdbcCursorItemReaderBuilder;
import org.springframework.batch.item.support.SynchronizedItemStreamReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import javax.sql.DataSource;

@Slf4j
@Configuration
@EnableBatchProcessing
public class JobConfiguration {
    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;
    private final DataSource dataSource;

    @Autowired
    public JobConfiguration(JobBuilderFactory jobBuilderFactory, StepBuilderFactory stepBuilderFactory, DataSource dataSource) {
        this.jobBuilderFactory = jobBuilderFactory;
        this.stepBuilderFactory = stepBuilderFactory;
        this.dataSource = dataSource;
    }


    @Bean
    public Job testJob(Step testStep) {
        return jobBuilderFactory.get("testJob")
                .incrementer(new RunIdIncrementer())
                .start(testStep)
                .build();
    }


    @Bean
    public Step testStep(WriterListener writerListener) {
        var reader = new SynchronizedItemStreamReader<String>();
        reader.setDelegate(itemReader());
        return stepBuilderFactory.get("testStep")
                .<String, String>chunk(4)
                .reader(reader)
                .processor(itemProcessor())
                .writer(itemWriter())
                .taskExecutor(taskExecutor())
                .listener(writerListener)
                .build();
    }

    @Bean
    @StepScope
    public JdbcCursorItemReader<String> itemReader() {
        return new JdbcCursorItemReaderBuilder<String>()
                .dataSource(dataSource)
                .name("itemReader")
                .sql("select id from test")
                .rowMapper((rs, rowNum) -> rs.getString(1))
                .verifyCursorPosition(false)
                .build();
    }

    @Bean
    @StepScope
    public JdbcBatchItemWriter<String> itemWriter() {
        return new JdbcBatchItemWriterBuilder<String>()
                .dataSource(dataSource)
                .sql("delete from test where id in (:ids)")
                .itemSqlParameterSourceProvider(items -> new MapSqlParameterSource("ids", items))
                .build();
    }

    @Bean
    @StepScope
    public ItemProcessor<String, String> itemProcessor() {
        return new ItemProcessor<String, String>() {
            @Override
            public String process(String item) throws Exception {
                log.info(item);
                return item;
            }
        };
    }


    @Bean
    public TaskExecutor taskExecutor() {
        var executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(3);
        executor.setThreadNamePrefix("Thread N-> :");
        return executor;
    }
}
