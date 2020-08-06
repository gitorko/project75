package com.demo.project75;

import java.util.stream.IntStream;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import com.querydsl.core.types.dsl.StringExpression;
import com.querydsl.core.types.dsl.StringPath;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.querydsl.binding.QuerydslBinderCustomizer;
import org.springframework.data.querydsl.binding.QuerydslBindings;
import org.springframework.data.querydsl.binding.QuerydslPredicate;
import org.springframework.data.querydsl.binding.SingleValueBinding;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
public class Application {

    @Autowired
    CustomerRepository customerRepository;

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @EventListener
    public void onStartSeedData(ContextRefreshedEvent event) {
        //Insert test data
        IntStream.range(0,5).forEach(i -> {
            customerRepository.save(Customer.builder()
                    .firstName("firstname_" + i)
                    .lastName("lastname " + i)
                    .age(30)
                    .email("email@email.com")
                    .build());
        });
    }
}

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
class Customer {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private int age;
}

@RestController
class HomeController{

    @Autowired
    CustomerRepository customerRepository;

    @RequestMapping(method = RequestMethod.GET, value = "/users")
    public Iterable<Customer> findAllByWebQuerydsl(
            @QuerydslPredicate(root = Customer.class,
                                bindings = CustomerBinderCustomizer.class) com.querydsl.core.types.Predicate predicate) {
        return customerRepository.findAll(predicate);
    }
}

interface CustomerRepository extends JpaRepository<Customer, Long>, QuerydslPredicateExecutor<Customer> {
}

class CustomerBinderCustomizer implements QuerydslBinderCustomizer<QCustomer> {

    @Override
    public void customize(QuerydslBindings querydslBindings, QCustomer qCustomer) {
        querydslBindings.including(
                        qCustomer.id,
                        qCustomer.firstName,
                        qCustomer.lastName,
                        qCustomer.age,
                        qCustomer.email);

        // Allow case-insensitive partial searches on all strings.
        querydslBindings.bind(String.class).first((SingleValueBinding<StringPath, String>) StringExpression::containsIgnoreCase);
    }
}