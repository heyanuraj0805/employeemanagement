package main.model;

/**
 * Represents a company department.
 */
public class Department {

    private int    id;
    private String name;
    private String description;

    public Department() {}

    public Department(int id, String name, String description) {
        this.id          = id;
        this.name        = name;
        this.description = description;
    }

    public int    getId()                    { return id;          }
    public void   setId(int id)              { this.id = id;       }

    public String getName()                  { return name;        }
    public void   setName(String n)          { this.name = n;      }

    public String getDescription()           { return description; }
    public void   setDescription(String d)   { this.description=d; }

    @Override public String toString() { return name; }
}
