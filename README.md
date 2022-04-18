# Spring Data JPA学习笔记

## 简介

**Spring Data JPA**是Spring Data系列的一部分，可以轻松实现基于JPA的存储库。本模块涉及**增强对基于JPA的数据访问层的支持**。它使构建使用数据访问技术的Spring驱动应用程序变得更加容易。

相当长一段时间以来，实现应用程序的数据访问层一直很麻烦。不得不编写太多的样板代码来执行简单的查询以及执行分页和审计。Spring Data JPA旨在通过将工作量减少到实际需要的数量来显著**改善数据访问层的实现**。作为开发人员，在编写存储库接口，包括自定义查找器方法时，Spring将自动提供实现。

## 实体与实体管理器

JPA提供了一种简单高效的方式来管理Java对象（POJO）到关系数据库的映射，此类Java对象称为**JPA实体**或简称实体。实体通常与底层数据库中的单个关系表相关联，每个实体的实例表示数据库表格中的某一行。

实体管理器（EntityManager）用于管理系统中的实体，它是实体与数据库之间的桥梁，通过调用实体管理器的相关方法可以把实体持久化到数据库中，同时也可以把数据库中的记录打包成实体对象。

### 实体状态分类

#### 临时状态（Transient）

实体对象刚创建出来的状态，实际上就是new了一个普通的实体，**没有调用持久化过程**。此时数据库中没有该对象的信息，该对象的ID属性也为空。如果没有被持久化，程序退出时临时状态的对象信息将丢失。

An object that is newly created and has never been associated with JPA *Persistence Context* (hibernate session) is considered to be in the New (Transient) state. The data of objects in this state is not stored in the database.

```java
Student student = new Student("email@example.com");
```

**NOTE:** 在使用EntityBuilder时，需要注意id字段，如果Entity中的id字段使用了@GeneratedValue注解，那么手动setId时，会将处于Transitent状态的对象变为detached状态，此时如果用entityManager.persist此对象，会抛出javax.persistence.PersistenceException.

#### 托管状态（Managed）

临时状态的实体在调用persist()方法后，实体状态改变为托管状态，该实体的任何属性改动都会导致数据库记录的改动。由于还在Session中，持久化状态的对象可以执行任何有关数据库的操作，例如获取集合属性的值等。

An Object that is associated with *persistence context* (hibernate session) are in Persistent state. Any changes made to objects in this state are automatically propagated to databases without manually invoking `persist`/`merge`/`remove`.

```java
Student student = new Student("email@example.com");

// Make changes and see if the data is updated automatically
EntityManager entityManager = entityManagerFactory.createEntityManager();
EntityTransaction tx = entityManager.getTransaction();
tx.begin();
entityManager.persist(student);
//purposely made changes - did not manually update
student.setEmail("updated_email@gmail.com");
Long persistedId = student.getId();
tx.commit();
entityManager.close();
// Test if the email address was updated
entityManager = entityManagerFactory.createEntityManager();
tx = entityManager.getTransaction();
tx.begin();
student = entityManager.find(Student.class, persistedId);
tx.commit();
entityManager.close();
System.out.println("Persisted Student: " + student);
```

#### 游离状态（Detached）

持久化状态的实体被commit到数据库后，即事务提交之后，实体状态就立即变为游离态。因为事务已经提交了，此时实体的属性如果有改变，也不会同步到数据库，实体已经不在持久化上下文中。

An Object becomes detached when the currently running *Persistence Context* is closed. Any changes made to detached objects are no longer automatically propagated to the database.

```java
tx.begin();
student = entityManager.find(Student.class, persistedId);
tx.commit();
entityManager.close();
System.out.println("Persisted Student: " + student);
student.setEmail("updated_again@gmail.com");// not changed in database

// Use merge(T entity) from javax.persistence.EntityManager to synchronise the changes made to a detached object with the database.
entityManager = entityManagerFactory.createEntityManager();
tx = entityManager.getTransaction();
tx.begin();
student = entityManager.merge(student);
tx.commit();
entityManager.close();
System.out.println("Persisted Student: " + student);

```

#### 删除状态（Removed）

对象处于删除状态，有id值，尚且和Persistence Context有关联，但是已经准备好从数据库中删除。

As the name suggests, removed objects are deleted from the database. JPA provides `entityManager.remove(object);` method to remove an entity from the database.

**NOTE:** 只有Managed状态的对象才能被删除, 如果删除Detached状态的对象，则会产生异常`java.lang.IllegalArgumentException: Removing a detached instance`.

```java
//Remove an Object from the database
entityManager = entityManagerFactory.createEntityManager();
tx = entityManager.getTransaction();
tx.begin();
student = entityManager.find(Student.class, persistedId);
entityManager.remove(student);
tx.commit();
entityManager.close();
```

### 实体状态转化

实体状态转化如图所示：

![实体状态转化图](https://i.stack.imgur.com/qMzDt.png)

不同的状态执行不同的操作会有不同的结果：

| 操作    | 新建状态 | 托管状态 | 游离状态 | 删除状态 |
| ------- | -------- | -------- | -------- | -------- |
| persist | 托管状态 | 托管状态 | 抛出异常 | 托管状态 |
| merge   | 托管状态 | 托管状态 | 托管状态 | 抛出异常 |
| refresh | 抛出异常 | 托管状态 | 抛出异常 | 抛出异常 |
| remove  | 新建状态 | 删除状态 | 抛出异常 | 删除状态 |

## 实体之间的关联查询

在关联查询之前，我们需要定义实体与实体之间的关系，这时就离不开关系映射。

### 关系映射

对象关系映射（Object relational mapping）是指通过将对象状态映射到数据库列，来开发和维护对象和关系数据库之间的关系。它能够轻松处理（执行）各种数据库操作，如插入、更新、删除等。

关系映射的注解如下：

|    注解     |                             说明                             |
| :---------: | :----------------------------------------------------------: |
| @JoinColumn | 指定一个实体组织或实体集合。多用在“多对一”和“一对多”的关联中。 |
|  @OneToOne  |                  定义表之间“一对一”的关系。                  |
| @OneToMany  |                  定义表之间“一对多”的关系。                  |
| @ManyToOne  |                  定义表之间“多对一”的关系。                  |
| @ManyToMany |                  定义表之间“多对多”的关系。                  |

下面将简单介绍如何使用Spring Data JPA处理多张数据库表之间的关联关系。

### `@OneToOne`一对一映射

一对一映射通常适用于主表和详情表的关联关系。

例如：书本和书本详细信息

书本Entity及对应的repository：

```java
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Book {
    @Id
    @GeneratedValue
    private Integer id;

    private String name;

    @OneToOne(cascade = {CascadeType.PERSIST,CascadeType.REMOVE})
    @JoinColumn(name="detailId",referencedColumnName = "id")
    private BookDetail bookDetail;
}

// repository
public interface BookRepository extends JpaRepository<Book,Integer> {
    Book findByName(String name);
}
```

书本详细信息Entity及对应repository：

```java
@Entity(name = "book_detail")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class BookDetail {

    @Id
    @GeneratedValue
    private Integer id;

    @Column(name = "number_of_pages")
    private Integer numberOfPages;

    @OneToOne(mappedBy = "bookDetail")
    private Book book;
}

// repository
public interface BookDetailRepository  extends JpaRepository<BookDetail, Integer>{
    BookDetail findByNumberOfPages(Integer numberOfPages);
}
```

@JoinColumn

- name：当前表的字段
- referencedColumnName：引用表对应的字段，如果不注明，默认就是引用表的主键

### `@OneToMany` 一对多映射

在处理一对多关系时候，通常有两种方式（Unidirectional，Bidirectional）

其中，使用双向映射（Bidirectional）是效率最高的，即同时使用`@OneToMany`和`@ManyToOne`，能产生最少的sql语句。

如果使用单向映射(@OneToMany)，可能会产生中间表。

一的一方：

```java
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Post {

    @Id
    @GeneratedValue
    private Long id;

    private String title;

    @OneToMany(
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            mappedBy = "post"
    )
    private Set<PostComment> comments = new HashSet<>();

    public void addComment(PostComment comment) {
        comments.add(comment);
        comment.setPost(this);
    }

    public void removeComment(PostComment comment) {
        comments.remove(comment);
        comment.setPost(null);
    }
}
```

多的一方：

```java
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
public class PostComment {

    @Id
    @GeneratedValue
    private Long id;

    private String review;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", referencedColumnName = "id")// optional
    private Post post;

    @Override
    public String toString() {
        return "PostComment{" +
                "id=" + id +
                ", review='" + review + '\'' +
                ", post=" + post +
                '}';
    }
}
```

如果关联关系时双向的，mappedBy属性必须用来标注，在拥有关联关系的实体一方中表示关系的字段名，也就是使用mappedBy属性是不维护关联关系的一方（少的一方，即使用@OneToMany的一方），值是拥有关联关系一方中标识关系的字段名。

**NOTE:** 

1. 使用了mappedBy属性后，不能在使用@JoinColumn注解，会抛异常
2. @ManyToOne 没有mappedBy属性
3. `@OneToOne / @ManyToMany`都是在实体的字段上表示对应的关联关系，在表示双向关联关系时候，都必须使用mappedBy属性

### `@ManyToMany` 多对多映射

多对多关联关系中只能通过中间表的方式进行映射。

单向映射：

@ManyToMany 注解用于关系的发出端
同时关系的发出端--定义一个集合类型的接收端的字段属性；
关系的接收端，不需要做任何定义；

双向映射：

@ManyToMany 注解用于关系的发出端和接收端
同时关系的发出端和接收端--定义一个集合类型的接收端的字段属性；
关系的接收端，@ManyToMany(mappedBy='集合类型发出端实体的字段名称')；

```java
@Entity
@Data
class Student {

    @Id
    Long id;

    @ManyToMany
    @JoinTable(
            name = "course_like",
            joinColumns = @JoinColumn(name = "student_id"),
            inverseJoinColumns = @JoinColumn(name = "course_id"))
    Set<Course> likedCourses;

}
```

```java
@Entity
@Data
class Course {

    @Id
    Long id;

    @ManyToMany(mappedBy = "likedCourses")
    Set<Student> likes;
}
```

**NOTE:** 因为在数据库中的多对多关系并没有拥有者的概念。因此我们也可以配置关联表在Course类中，在Student中配置引用。

## FetchMode

FetchMode有三个选项：join ,select ,subselect。

```java
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Customer {

    @Id
    @GeneratedValue
    private Long id;

    private String name;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "customer")
    @Fetch(value = FetchMode.SELECT)
    private Set<Order> orders = new HashSet<>();

    public void addOrder(Order order){
        orders.add(order);
        order.setCustomer(this);
    }
}
```

```java
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue
    private Long id;

    private String name;

    @ManyToOne
    @JoinColumn(name = "customer_id", referencedColumnName = "id")
    private Customer customer;

    @Override
    public String toString() {
        return "Order{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", customer=" + customer +
                '}';
    }
}
```

```java
Customer savedCustomer = customerRepository.findById(Long.valueOf(1)).get();
Set<Order> orders = savedCustomer.getOrders();
```

用不同的FetchMode，执行上述语句，查看hibernate的执行情况。

### FetchMode.JOIN

使用left outer join的方式来加载，只有一条sql语句：

```sql
Hibernate: 
    select ...
    from
        customer customer0_ 
    left outer join
        orders orders1_ 
            on customer0_.id=orders1_.customer_id 
    where
        customer0_.id=?
```

### FetchMode.SELECT

执行N+1条语句，即一条查询customer，N条查询对应的orders（如果关联的order有N条）

```sql
Hibernate: 
    select ...from customer
    where customer0_.id=?
Hibernate: 
    select ...from orders
    where orders0_.customer_id=?
```

在此情况下，还可以使用@BatchSize(size=10)来进行优化，加载时候会根据size的大小来进行加载。

### FetchMode.SUBSELECT

执行2条语句，第一条查询customer，第二条使用id in (…..)查询出所有关联的数据

```sql
Hibernate: 
    select ...
    from customer customer0_
Hibernate: 
    select ...
    from
        orders orders0_ 
    where
        orders0_.customer_id in (
            select
                customer0_.id 
            from
                customer customer0_
        )
```

**NOTE:** 

1. @Fetch(FetchMode.JOIN)

   对数据库而言，采用联合查询方式（JOIN方式），只需要一条SQL语句,并且会自动抓取所有的相关记录,完全忽略Lazy。

   但在对批量数据的处理中(例如查询得到了10条数据，每条数据又关联了一个实体)，依旧会每个实体执行一次JOIN查询，@BatchSize注解对此种方式完全无用。

2. @Fetch(FetchMode.SELECT)

   在单条实体数据的抓取中，需要执行两条SQL语句，并且此种数据的抓取必须包含在事务中。

   但在批量数据的处理中，此种方式会显著提高性能，结合BatchSize同时使用，会按照Size的大小片断抓取关联数据，能显著提高数据抓取性能。

3. @Fetch(FetchMode.SUBSELECT)

   在单条实体数据的处理中，与第二种方式相似，依然需要两条数据。

   但在批量数据的处理中，性能最佳，只抓取一次,就能获取所有的记录。
   
4. 在使用Spring Data JPA默认提供的查询方法时，可能涉及到自动优化的问题：

   `findById`等查询出单个数据的方法时，`SELECT`和`SUBSELECT`方法都没有N+1问题（都只Fetch一次）；

   而当使用查询多个数据的方法时（例如`findAll`），在Fetch的时候则`SELECT`存在N+1问题，而`SUBSELECT`只Fetch一次。

5. 如果`FetchType = EAGER`，那么`FetchMode = JOIN`，如果`FetchType = LAZY`，那么`FetchMode = SELECT`。



# References

[1]: https://spring.io/projects/spring-data-jpa	"Spring Data JPA官方文档"
[2]: https://jstobigdata.com/jpa/different-states-of-an-object-in-jpa/	"Different Entity states in JPA and Hibernate"
[3]: https://www.cnblogs.com/zjfjava/p/9064092.html	"Spring Data JPA实体详解"
[4]: https://www.baeldung.com/hibernate-fetchmode	"FetchMode in Hibernat"
