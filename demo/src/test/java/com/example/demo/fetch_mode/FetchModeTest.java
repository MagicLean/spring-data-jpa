package com.example.demo.fetch_mode;

import com.example.demo.configuration.IntegrationBaseTest;
import com.example.demo.fetch_mode.entity.Customer;
import com.example.demo.fetch_mode.entity.Order;
import com.example.demo.fetch_mode.repository.CustomerRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;

class FetchModeTest extends IntegrationBaseTest {

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    EntityManager entityManager;

    @Test
    @Transactional
    void should_test_fetch_mode_when_fetch_one() {
        saveCustomers();
        entityManager.clear();
        // 如果只有单条数据的查询，那么Fetch的时候都是直接查询出来，存在N+1问题（无论SUBSELECT还是SELECT），JOIN则仍然为联表查询
        Customer customer1 = customerRepository.findById(1L).get();
        System.out.println(String.format("Fetch the collection of orders for Customer %s",
                customer1.getName()));
        Set<Order> orders = customer1.getOrders();
        for (Order order : orders) {
            System.out.println(order.toString());
        }
        Customer customer2 = customerRepository.findById(2L).get();
        System.out.println(String.format("Fetch the collection of orders for Customer %s",
                customer2.getName()));
        Set<Order> orders2 = customer2.getOrders();
        for (Order order : orders2) {
            System.out.println(order.toString());
        }
    }

    @Test
    @Transactional
    void should_test_fetch_mode_when_fetch_many() {
        saveCustomers();
        entityManager.clear();
        // 如果有多条customer数据，同时查询出多个customer时，Fetch的时候会根据FetchMode进行改变（SUBSELECT会通过in的方式，没有N+1问题）
        List<Customer> customers = customerRepository.findAll();
        for (Customer customer : customers) {
            System.out.println(String.format("Fetch the collection of orders for Customer %s",
                    customer.getName()));
            Set<Order> orders = customer.getOrders();
            for (Order order : orders) {
                System.out.println(order.toString());
            }
        }
    }

    public void saveCustomers() {
        Customer customer = new Customer();
        customer.setName("customer1");
        Order order1 = new Order();
        order1.setName("Order1");
        Order order2 = new Order();
        order2.setName("Order2");
        Order order3 = new Order();
        order3.setName("Order3");
        customer.addOrder(order1);
        customer.addOrder(order2);
        customer.addOrder(order3);
        Customer customer2 = new Customer();
        customer2.setName("customer2");
        Order order4 = new Order();
        order4.setName("Order1");
        Order order5 = new Order();
        order5.setName("Order2");
        Order order6 = new Order();
        order6.setName("Order3");
        customer2.addOrder(order4);
        customer2.addOrder(order5);
        customer2.addOrder(order6);
        customerRepository.saveAndFlush(customer);
        customerRepository.saveAndFlush(customer2);
    }
}
