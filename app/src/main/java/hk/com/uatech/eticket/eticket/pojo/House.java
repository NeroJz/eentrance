package hk.com.uatech.eticket.eticket.pojo;

public class House {

    private Name name;
    private String id;

    public Name getName ()
    {
        return name;
    }

    public void setName (Name name)
    {
        this.name = name;
    }

    public String getId ()
    {
        return id;
    }

    public void setId (String id)
    {
        this.id = id;
    }

    @Override
    public String toString() {
        return "ClassPojo [name = " + name + " id = " + id + "]";
    }
}
