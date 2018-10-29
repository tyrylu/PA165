package cz.fi.muni.pa165.tasks;

import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceUnit;
import javax.validation.ConstraintViolationException;

import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import cz.fi.muni.pa165.PersistenceSampleApplicationContext;
import cz.fi.muni.pa165.entity.Category;
import cz.fi.muni.pa165.entity.Product;

 
@ContextConfiguration(classes = PersistenceSampleApplicationContext.class)
public class Task02 extends AbstractTestNGSpringContextTests {

	@PersistenceUnit
	private EntityManagerFactory emf;
private Category electro;
private Category kitchen;
private Product light;
private Product robot;
private Product plate;

	
	private Category createCategory(String name, EntityManager em) {
		Category cat = new Category();
		cat.setName(name);;
	em.persist(cat);
		return cat;
	}
	private Product createProduct(String name, EntityManager em) {
		Product prod = new Product();
		prod.setName(name);;
	em.persist(prod);
		return prod;
	}
	
	
	@BeforeClass
	public void beforeTestClass() {
		EntityManager em = emf.createEntityManager();
		em.getTransaction().begin();
				electro = createCategory("Electro", em);
				kitchen = createCategory("Kitchen", em);
		light = createProduct("Flashlight", em);
				robot = createProduct("Kitchen robot", em);
				plate = createProduct("Plate", em);
				kitchen.addProduct(plate);;
				kitchen.addProduct(robot);;
				electro.addProduct(light);
				electro.addProduct(robot);
				em.getTransaction().commit();
				em.close();
		
		
	}
	
	
	private void assertContainsCategoryWithName(Set<Category> categories,
			String expectedCategoryName) {
		for(Category cat: categories){
			if (cat.getName().equals(expectedCategoryName))
				return;
		}
			
		Assert.fail("Couldn't find category "+ expectedCategoryName+ " in collection "+categories);
	}
	private void assertContainsProductWithName(Set<Product> products,
			String expectedProductName) {
		
		for(Product prod: products){
			if (prod.getName().equals(expectedProductName))
				return;
		}
			
		Assert.fail("Couldn't find product "+ expectedProductName+ " in collection "+products);
	}

	// Note that the tests could, in theory, be collapsed to an one-liner, but the heavyweight method would probably require reflection to get to the correct Set of related items.
	@Test
	public void kitchenIsCorrect() {
		EntityManager em = emf.createEntityManager();
		Category cat = em.find(Category.class,  kitchen.getId());
		assertContainsProductWithName(cat.getProducts(), "Kitchen robot");
		assertContainsProductWithName(cat.getProducts(), "Plate");
	}
	
	@Test
	public void electroIsCorrect() {
		EntityManager em = emf.createEntityManager();
		Category cat = em.find(Category.class,  electro.getId());
		assertContainsProductWithName(cat.getProducts(), "Kitchen robot");
		assertContainsProductWithName(cat.getProducts(), "Flashlight");
	}
	@Test
	public void plateIsCorrect() {
		EntityManager em = emf.createEntityManager();
		Product prod = em.find(Product.class, plate.getId());
		assertContainsCategoryWithName(prod.getCategories(), "Kitchen");
			}
	
	@Test
	public void FlashlightIsCorrect() {
		EntityManager em = emf.createEntityManager();
		Product prod = em.find(Product.class, light.getId());
		assertContainsCategoryWithName(prod.getCategories(), "Electro");
			}
	@Test
	public void robotIsCorrect() {
		EntityManager em = emf.createEntityManager();
		Product prod = em.find(Product.class, robot.getId());
		assertContainsCategoryWithName(prod.getCategories(), "Kitchen");
		assertContainsCategoryWithName(prod.getCategories(), "Electro");		
	}
	@Test(expectedExceptions=ConstraintViolationException.class)
	public void doesNotSaveNullName() {
		EntityManager em = emf.createEntityManager();
		Product p = new Product();
		em.getTransaction().begin();
		em.persist(p);
		em.getTransaction().commit();
	}
}
