package com.example.demo.fetch_mode;

import com.example.demo.configuration.IntegrationBaseTest;
import com.example.demo.fetch_mode.entity.Customer;
import com.example.demo.fetch_mode.entity.Order;
import com.example.demo.fetch_mode.repository.CustomerRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;

class FetchModeTest extends IntegrationBaseTest {

    @Autowired
    private CustomerRepository customerRepository;

    @Test
    void should_test_fetch_mode() {
        saveCustomer();
        Customer savedCustomer = customerRepository.findById(Long.valueOf(1)).get();
        Set<Order> orders = savedCustomer.getOrders();
        assertFalse(orders.isEmpty());
    }

    public void saveCustomer() {
        Customer customer = new Customer();
        Order order1 = new Order();
        order1.setName("Order1");
        Order order2 = new Order();
        order2.setName("Order2");
        Order order3 = new Order();
        order3.setName("Order3");
        customer.addOrder(order1);
        customer.addOrder(order2);
        customer.addOrder(order3);
        customerRepository.saveAndFlush(customer);
    }
}
