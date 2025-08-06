package com.example.all_in_one.services;

import com.example.all_in_one.models.Customer;
import com.example.all_in_one.clients.orderservice.Order;
import com.example.all_in_one.repositories.CustomerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
public class CustomerServiceTest {

    @MockBean
    private CustomerRepository customerRepository;

    @MockBean
    private OrderService orderService;

    private CustomerService customerService;

    @BeforeEach
    public void setUp() {
        var customer = Customer.builder().firstName("John").lastName("Doe").build();
        var todayDate = LocalDate.now();
        var order = new Order(24L, todayDate, todayDate.plusDays(3L), null, List.of("item1", "item2"));

        when(customerRepository.findById(anyLong())).thenReturn(Mono.just(customer));
        when(orderService.fetchCustomerOrder(anyLong())).thenReturn(Mono.just(order));
        
        /*
        //If a private field to be set with some value It can be done in this Reflection way
        import org.springframework.test.util.ReflectionTestUtils;
        ReflectionTestUtils.setField(customerService, "privatePublicFileName", "value");
        */
        
        customerService = new CustomerService(customerRepository, orderService);
    }

    @Test
    void fetchCustomerTest() {
        customerService
                .fetchCustomer(1L)
                .as(StepVerifier::create)
                .consumeNextWith(customer -> {
                    assert customer.getFirstName().equals("John");
                    assert customer.getLastName().equals("Doe");
                    assert customer.getOrder().id().equals(24L);
                    assert customer.getOrder().placedAt().equals(LocalDate.now());
                    assert customer.getOrder().expectedDeliveryDate().equals(LocalDate.now().plusDays(3L));
                    assert customer.getOrder().deliveredDate() == null;
                    assert customer.getOrder().items().equals(List.of("item1", "item2"));
                })
                .then(() -> {
                    Mockito.verify(customerRepository, Mockito.times(1)).findById(1L);
                    Mockito.verify(orderService, Mockito.times(1)).fetchCustomerOrder(1L);
                })
                .verifyComplete();



    }

}
