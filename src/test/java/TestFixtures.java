import com.microsoft.playwright.*;
import org.junit.jupiter.api.*;

import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

// Subclasses will inherit PER_CLASS behavior.
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class TestFixtures {
    // Shared between all tests in the class.
    Playwright playwright;
    Browser browser;

    // New instance for each test method.
    BrowserContext context;
    Page page;

    @BeforeAll
    void launchBrowser() {
        playwright = Playwright.create();
        browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(false));
        context = browser.newContext(new Browser.NewContextOptions().setRecordVideoDir(Paths.get("videos/")));
        // Make sure to close, so that videos are saved.
    }

    @AfterAll
    void closeBrowser() {
        playwright.close();
    }

    @BeforeEach
    void createContextAndPage() {
        context = browser.newContext();
        // Start tracing before creating / navigating a page.
        context.tracing().start(new Tracing.StartOptions()
                .setScreenshots(true)
                .setSnapshots(true)
                .setSources(true));
        page = context.newPage();

    }

    @AfterEach
    void closeContext() {
        // Stop tracing and export it into a zip archive.
        context.tracing().stop(new Tracing.StopOptions()
                .setPath(Paths.get("trace.zip")));
        context.close();
    }
}

class Test1 extends TestFixtures {
    @Test
    void shouldClickButton() {
        page.navigate("data:text/html,<script>var result;</script><button onclick='result=\"Clicked\"'>Go</button>");
        page.locator("button").click();
        assertEquals("Clicked", page.evaluate("result"));
    }

    @Test
    void shouldCheckTheBox() {
        page.setContent("<input id='checkbox' type='checkbox'></input>");
        page.locator("input").check();
        assertTrue((Boolean) page.evaluate("() => window['checkbox'].checked"));
    }

    @Test
    void shouldSearchWiki() {
        page.navigate("https://www.wikipedia.org/");
        page.locator("input[name=\"search\"]").click();
        page.locator("input[name=\"search\"]").fill("playwright");
        page.locator("input[name=\"search\"]").press("Enter");
        assertEquals("https://ru.wikipedia.org/w/index.php?go=Go&search=playwright&title=%D0%A1%D0%BB%D1%83%D0%B6%D0%B5%D0%B1%D0%BD%D0%B0%D1%8F:%D0%9F%D0%BE%D0%B8%D1%81%D0%BA&ns0=1", page.url());
    }
}

class Test2 extends TestFixtures {
    @Test
    void shouldReturnInnerHTML() {
        page.setContent("<div>hello</div>");
        assertEquals("hello", page.innerHTML("css=div"));
    }

    @Test
    void shouldClickButton() {
        Page popup = page.waitForPopup(() -> {
            page.evaluate("window.open('about:blank');");
        });
        assertEquals("about:blank", popup.url());
    }
}
