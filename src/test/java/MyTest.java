import java.net.MalformedURLException;
import java.net.URL;

import org.openqa.demo.grid.logging.NewBrowserRequestEvent;
import org.openqa.demo.grid.logging.SeleniumGridEvent;
import org.openqa.demo.grid.logging.TimeoutEvent;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.testng.annotations.Test;

public class MyTest {

	@Test(invocationCount=10,threadPoolSize=10)
	public void test() throws MalformedURLException {
		DesiredCapabilities cap = DesiredCapabilities.firefox();
		cap.setVersion("4.0");
		cap.setCapability("user", System.getProperty("user.name"));
		WebDriver driver = new RemoteWebDriver(new URL("http://localhost:4444/wd/hub"), cap);
		driver.get("http://google.com");
		System.out.println(driver.getTitle());
		driver.quit();
	}
	
	@Test
	public void testUnknownUser() throws MalformedURLException {
		DesiredCapabilities cap = DesiredCapabilities.firefox();
		WebDriver driver = new RemoteWebDriver(new URL("http://localhost:4444/wd/hub"), cap);
		driver.get("http://google.com");
		System.out.println(driver.getTitle());
		driver.quit();
	}
	
	@Test
	public void testWillTimeOut() throws MalformedURLException {
		DesiredCapabilities cap = DesiredCapabilities.firefox();
		cap.setVersion("3.6");
		cap.setCapability("user", System.getProperty("user.name"));
		WebDriver driver = new RemoteWebDriver(new URL("http://localhost:4444/wd/hub"), cap);
		driver.get("http://google.com");
		System.out.println(driver.getTitle());
	}
	
	@Test(invocationCount=2)
	public void testWillTimeOutff4() throws MalformedURLException {
		DesiredCapabilities cap = DesiredCapabilities.firefox();
		cap.setVersion("4.0");
		cap.setCapability("user", System.getProperty("user.name"));
		WebDriver driver = new RemoteWebDriver(new URL("http://localhost:4444/wd/hub"), cap);
		driver.get("http://google.com");
		System.out.println(driver.getTitle());
	}
	
	
}
