package org.banshi.Config;

import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RazorpayConfig {

    private static final Logger logger = LoggerFactory.getLogger(RazorpayConfig.class);

    @Value("${razorpay.key_id}")
    private String keyId;

    @Value("${razorpay.key_secret}")
    private String keySecret;

    @Bean
    public RazorpayClient razorpayClient() {
        try {
            logger.info("Initializing RazorpayClient...");
            RazorpayClient client = new RazorpayClient(keyId, keySecret);
            logger.info("RazorpayClient initialized successfully");
            return client;
        } catch (RazorpayException e) {
            logger.error("Failed to initialize RazorpayClient. Please check your Razorpay credentials.", e);

            // Instead of crashing, throw an IllegalStateException
            throw new IllegalStateException("Could not initialize RazorpayClient due to invalid configuration", e);
        }
    }
}
