package com.examplatform.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * Written-module ও Doubt-module এর AI service (Gemini, ImageKit upload) গুলো
 * RestTemplate ব্যবহার করে বাইরের API কল করে। মূল project-এ কোনো RestTemplate bean
 * define করা ছিল না (নতুন backend built-in RestTemplate bean ছাড়াই ছিল), তাই merge করার
 * সময় এখানে একটা সাধারণ bean যুক্ত করা হলো।
 */
@Configuration
public class RestTemplateConfig {

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
