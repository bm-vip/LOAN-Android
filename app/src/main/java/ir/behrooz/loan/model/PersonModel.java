package ir.behrooz.loan.model;

import java.util.ArrayList;
import java.util.List;

import ir.behrooz.loan.entity.PersonEntity;

public class PersonModel {
    private Long id;
    private String fullName;
    private boolean checked;

    public PersonModel(Long id, String fullName, boolean checked) {
        this.id = id;
        this.fullName = fullName;
        this.checked = checked;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public boolean isChecked() {
        return checked;
    }

    public void setChecked(boolean checked) {
        this.checked = checked;
    }

    public static List<PersonModel> getModels(List<PersonEntity> personEntities) {
        List<PersonModel> personModels = new ArrayList<>();
        for (PersonEntity entity : personEntities) {
            personModels.add(getModel(entity));
        }
        return personModels;
    }

    public static String getIds(List<PersonModel> list){
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < list.size(); i++) {
            PersonModel item = list.get(i);
            builder.append(item.getId());
            if (i < list.size() - 1)
                builder.append(",");
        }
        return builder.toString();
    }

    public static PersonModel getModel(PersonEntity entity) {
        return new PersonModel(entity.getId(), String.format("%s %s", entity.getName(), entity.getFamily()), false);
    }

}
