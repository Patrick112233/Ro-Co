package de.th_rosenheim.ro_co.frontend.views.community;


import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.listbox.MultiSelectListBox;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import java.util.List;

import de.th_rosenheim.ro_co.frontend.components.avataritem.AvatarItem;
import org.vaadin.lineawesome.LineAwesomeIconUrl;

@PageTitle("Community")
@Route("community")
@Menu(order = 1, icon = LineAwesomeIconUrl.USER_TAG_SOLID)
@AnonymousAllowed
public class CommunityView extends Composite<VerticalLayout> {

    public CommunityView() {
        H1 h1 = new H1();
        MultiSelectListBox avatarItems = new MultiSelectListBox();
        getContent().setWidth("100%");
        getContent().getStyle().set("flex-grow", "1");
        h1.setText("Our Community");
        h1.setWidth("max-content");
        avatarItems.setWidth("100%");
        avatarItems.getStyle().set("flex-grow", "1");
        setAvatarItemsSampleData(avatarItems);
        getContent().add(h1);
        getContent().add(avatarItems);
    }

    private void setAvatarItemsSampleData(MultiSelectListBox multiSelectListBox) {
        record Person(String name, String profession) {
        }
        ;
        List<Person> data = List.of(new Person("Aria Bailey", "Endocrinologist"), new Person("Aaliyah Butler", "Nephrologist"), new Person("Eleanor Price", "Ophthalmologist"), new Person("Allison Torres", "Allergist"), new Person("Madeline Lewis", "Gastroenterologist"));
        multiSelectListBox.setItems(data);
        multiSelectListBox.setRenderer(new ComponentRenderer(item -> {
            AvatarItem avatarItem = new AvatarItem();
            avatarItem.setHeading(((Person) item).name);
            avatarItem.setDescription(((Person) item).profession);
            avatarItem.setAvatar(new Avatar(((Person) item).name));
            return avatarItem;
        }));
    }
}
