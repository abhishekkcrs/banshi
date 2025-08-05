package org.banshi.Services;

import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import org.banshi.Dtos.OrderResponse;
import org.banshi.Dtos.VerifyPaymentRequest;
import org.banshi.Entities.Enums.PaymentStatus;
import org.banshi.Entities.PaymentTransaction;
import org.banshi.Entities.User;
import org.banshi.Repositories.PaymentTransactionRepository;
import org.banshi.Repositories.UserRepository;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.time.LocalDateTime;
import java.util.Base64;

@Service
public class PaymentService {

    private static final Logger logger = LoggerFactory.getLogger(PaymentService.class);

    private final RazorpayClient razorpayClient;
    private final PaymentTransactionRepository paymentRepo;
    private final UserRepository userRepo;
    private final String secret;

    public PaymentService(RazorpayClient razorpayClient,
                          PaymentTransactionRepository paymentRepo,
                          UserRepository userRepo,
                          @Value("${razorpay.key_secret}") String secret) {
        this.razorpayClient = razorpayClient;
        this.paymentRepo = paymentRepo;
        this.userRepo = userRepo;
        this.secret = secret;
        logger.info("PaymentService initialized successfully");
    }

    public OrderResponse createOrder(Long userId, Double amount) throws RazorpayException {
        logger.info("Creating Razorpay order for userId={}, amount={}", userId, amount);

        User user = userRepo.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));

        if (amount == null || amount <= 0) {
            throw new IllegalArgumentException("Amount must be greater than zero.");
        }

        JSONObject options = new JSONObject();
        options.put("amount", amount * 100); // paise
        options.put("currency", "INR");
        options.put("payment_capture", 1);

        Order order = razorpayClient.orders.create(options);

        PaymentTransaction transaction = PaymentTransaction.builder()
                .user(user)
                .razorpayOrderId(order.get("id"))
                .amount(amount)
                .status(PaymentStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        paymentRepo.save(transaction);

        logger.info("Order created successfully: orderId={}, userId={}", order.get("id"), userId);

        return new OrderResponse(order.get("id"), amount, "INR");
    }

    public boolean verifyPayment(VerifyPaymentRequest request) throws Exception {
        logger.info("Verifying payment for orderId={}, paymentId={}", request.getRazorpayOrderId(), request.getRazorpayPaymentId());

        String data = request.getRazorpayOrderId() + "|" + request.getRazorpayPaymentId();
        String expectedSignature = hmacSHA256(data, secret);

        PaymentTransaction transaction = paymentRepo.findByRazorpayOrderId(request.getRazorpayOrderId())
                .orElseThrow(() -> new RuntimeException("Transaction not found"));

        if (expectedSignature.equals(request.getRazorpaySignature())) {
            transaction.setRazorpayPaymentId(request.getRazorpayPaymentId());
            transaction.setRazorpaySignature(request.getRazorpaySignature());
            transaction.setStatus(PaymentStatus.SUCCESS);
            transaction.setUpdatedAt(LocalDateTime.now());
            paymentRepo.save(transaction);

            User user = transaction.getUser();
            user.setBalance(user.getBalance() + transaction.getAmount());
            userRepo.save(user);

            logger.info("Payment verified successfully: orderId={}, paymentId={}", request.getRazorpayOrderId(), request.getRazorpayPaymentId());
            return true;
        }

        transaction.setStatus(PaymentStatus.FAILED);
        transaction.setUpdatedAt(LocalDateTime.now());
        paymentRepo.save(transaction);

        logger.warn("Payment verification failed: orderId={}, paymentId={}", request.getRazorpayOrderId(), request.getRazorpayPaymentId());
        return false;
    }

    private String hmacSHA256(String data, String key) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        SecretKeySpec secretKeySpec = new SecretKeySpec(key.getBytes(), "HmacSHA256");
        mac.init(secretKeySpec);
        byte[] hash = mac.doFinal(data.getBytes());
        return Base64.getEncoder().encodeToString(hash);
    }
}
