package org.delcom.ifs25024pbo2026springbootcashflow;

import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.component.page.Meta;
import com.vaadin.flow.component.page.Viewport;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@Meta(name = "description", content = "Cashflow: aplikasi pencatatan pemasukan dan pengeluaran keuangan pribadi berbasis Spring Boot dan Vaadin.")
@Viewport("width=device-width, initial-scale=1")
public class App implements AppShellConfigurator {

    public static void main(String[] args) throws Exception {
        DotEnv.load();
        SpringApplication.run(App.class, args);
    }
}
