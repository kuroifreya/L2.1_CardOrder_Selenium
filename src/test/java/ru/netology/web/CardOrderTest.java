package ru.netology.web;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CardOrderTest {
    private WebDriver driver;

    @BeforeAll
    static void setUpAll() {
        WebDriverManager.chromedriver().setup();
    }

    @BeforeEach
    void setUp() {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--no-sandbox");
        options.addArguments("--headless");
        driver = new ChromeDriver(options);
        driver.get("http://localhost:9999");
    }

    @AfterEach
    void tearDown() {
        driver.quit();
        driver = null;
    }

    @ParameterizedTest
    @CsvSource({"Василий",//одно слово
            "Пётр Иванов",//буква ё
            "Анна-Мария Лих-Касинская",//пробелы и дефисы
            "Ю И",//короткие имя и фамилия
            "Валерия Марианна Дельфина Афродита Кассиопея Муза Стефани Штольц Валерия Марианна Дельфина Афродита Кассиопея Сиа Стефани Штольц",//128 символов, проверка границ
            "Валерия Марианна Дельфина Афродита Кассиопея Муза Стефани Штольц Валерия Марианна Дельфина Афродита Кассиопея Сиа Стефани Шольц",//127 символов, проверка границ
            "о"//1 символ, проверка границ
    })
    void shouldAcceptValidNames(String name) {
        driver.findElement(By.cssSelector("[data-test-id=name] input")).sendKeys(name);
        driver.findElement(By.cssSelector("[data-test-id=phone] input")).sendKeys("+79270000000");
        driver.findElement(By.className("checkbox__box")).click();
        driver.findElement(By.className("button")).click();
        String text = driver.findElement(By.cssSelector("[data-test-id=order-success]")).getText();
        assertEquals("Ваша заявка успешно отправлена! Наш менеджер свяжется с вами в ближайшее время.", text.trim());
    }

    @ParameterizedTest
    @CsvSource({"John",//латинские
            "Катя123",//содержит цифры
            "!№%:?*()",//спецсимволы
            // " ",""//пробел вместо имени → возникает ошибка инициализации теста. Не разобралась пока, как решить
            "-", //дефис вместо имен
            "あんどれい",//японскими буквами
            "Валерия Марианна Дельфина Афродита Кассиопея Муза Стефани Штольц Валерия Марианна Дельфина Афродита Кассиопея Сима Стефани Штольц"//129, проверка границ
    })
    void shouldNotAcceptInvalidNames(String name) {
        driver.findElement(By.cssSelector("[data-test-id=name] input")).sendKeys(name);
        driver.findElement(By.cssSelector("[data-test-id=phone] input")).sendKeys("+79270000000");
        driver.findElement(By.className("checkbox__box")).click();
        driver.findElement(By.className("button")).click();
        String text = driver.findElement(By.cssSelector("span[data-test-id=name].input_invalid .input__sub")).getText();
        assertEquals("Имя и Фамилия указаные неверно. Допустимы только русские буквы, пробелы и дефисы.", text.trim());
    }

    @Test
    void shouldAcceptValidPhones() {
        driver.findElement(By.cssSelector("[data-test-id=name] input")).sendKeys("Иван");
        driver.findElement(By.cssSelector("[data-test-id=phone] input")).sendKeys("+79270000000");
        driver.findElement(By.className("checkbox__box")).click();
        driver.findElement(By.className("button")).click();
        String text = driver.findElement(By.cssSelector("[data-test-id=order-success]")).getText();
        assertEquals("Ваша заявка успешно отправлена! Наш менеджер свяжется с вами в ближайшее время.", text.trim());
    }

    @ParameterizedTest
    @CsvSource({"+7927123456",//10 символов
            "+792756473281",//12 символов
            "79270000000",//нет +
            "телефон", "phone",
            "!@#$%^&*()_+"
    })
    void shouldNotAcceptPhones(String phone) {
        driver.findElement(By.cssSelector("[data-test-id=name] input")).sendKeys("Иван");
        driver.findElement(By.cssSelector("[data-test-id=phone] input")).sendKeys(phone);
        driver.findElement(By.className("checkbox__box")).click();
        driver.findElement(By.className("button")).click();
        String text = driver.findElement(By.cssSelector("span[data-test-id=phone].input_invalid .input__sub")).getText();

        assertEquals("Телефон указан неверно. Должно быть 11 цифр, например, +79012345678.", text);
    }

    @Test
    void shouldNotProceedWithUncheckedBox() {
        driver.findElement(By.cssSelector("[data-test-id=name] input")).sendKeys("Иван");
        driver.findElement(By.cssSelector("[data-test-id=phone] input")).sendKeys("+79272143657");
        driver.findElement(By.className("button")).click();

        boolean checked = driver.findElement(By.cssSelector("[data-test-id=agreement]")).getAttribute("class").contains("input_invalid");

        assertEquals(true, checked);
    }

    @Test
    void shouldNotProceedWithEmptyName() {
        driver.findElement(By.cssSelector("[data-test-id=phone] input")).sendKeys("+79871234567");
        driver.findElement(By.className("checkbox__box")).click();
        driver.findElement(By.className("button")).click();
        String text = driver.findElement(By.cssSelector("span[data-test-id=name].input_invalid .input__sub")).getText();

        assertEquals("Поле обязательно для заполнения", text.trim());
    }

    @Test
    void shouldNotProceedWithEmptyPhone() {

        driver.findElement(By.cssSelector("[data-test-id=name] input")).sendKeys("Валентин Юдашкин-Зверев");
        driver.findElement(By.className("checkbox__box")).click();
        driver.findElement(By.className("button")).click();
        String text = driver.findElement(By.cssSelector("span[data-test-id=phone].input_invalid .input__sub")).getText();

        assertEquals("Поле обязательно для заполнения", text.trim());
    }


}
