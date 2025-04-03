package de.th_rosenheim.ro_co.frontend;

import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;

@Route("")
public class MainView extends VerticalLayout {

    public MainView() {
        Span label = new Span("Hello World");

        /*Add Items to Layout*/
        add(label);
    }



}
