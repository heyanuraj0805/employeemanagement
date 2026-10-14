package main.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Represents a company employee.
 */
public class Employee {

    public enum Gender         { Male, Female, Other }
    public enum EmploymentType { FullTime, PartTime, Contract, Intern }
    public enum Status         { Active, Inactive, Resigned, Terminated }

    private int            id;
    private String         empCode;
    private String         firstName;
    private String         lastName;
    private Gender         gender;
    private LocalDate      dob;
    private String         email;
    private String         phone;
    private String         address;
    private int            departmentId;
    private String         departmentName;   // joined field
    private String         designation;
    private LocalDate      dateOfJoining;
    private EmploymentType employmentType;
    private Status         status;
    private LocalDateTime  createdAt;
    private LocalDateTime  updatedAt;

    public Employee() {}

    // ── Getters / Setters ───────────────────────────────────────────────────
    public int    getId()                    { return id;             }
    public void   setId(int id)              { this.id = id;          }

    public String getEmpCode()               { return empCode;        }
    public void   setEmpCode(String c)       { this.empCode = c;      }

    public String getFirstName()             { return firstName;      }
    public void   setFirstName(String fn)    { this.firstName = fn;   }

    public String getLastName()              { return lastName;       }
    public void   setLastName(String ln)     { this.lastName = ln;    }

    public String getFullName()              { return firstName + " " + lastName; }

    public Gender getGender()                { return gender;         }
    public void   setGender(Gender g)        { this.gender = g;       }

    public LocalDate getDob()                { return dob;            }
    public void      setDob(LocalDate d)     { this.dob = d;          }

    public String getEmail()                 { return email;          }
    public void   setEmail(String e)         { this.email = e;        }

    public String getPhone()                 { return phone;          }
    public void   setPhone(String p)         { this.phone = p;        }

    public String getAddress()               { return address;        }
    public void   setAddress(String a)       { this.address = a;      }

    public int    getDepartmentId()          { return departmentId;   }
    public void   setDepartmentId(int d)     { this.departmentId = d; }

    public String getDepartmentName()        { return departmentName; }
    public void   setDepartmentName(String n){ this.departmentName=n; }

    public String getDesignation()           { return designation;    }
    public void   setDesignation(String d)   { this.designation = d;  }

    public LocalDate      getDateOfJoining()          { return dateOfJoining;    }
    public void           setDateOfJoining(LocalDate d){ this.dateOfJoining = d; }

    public EmploymentType getEmploymentType()              { return employmentType;   }
    public void           setEmploymentType(EmploymentType e){ this.employmentType=e;}

    public Status getStatus()                { return status;         }
    public void   setStatus(Status s)        { this.status = s;       }

    public LocalDateTime getCreatedAt()      { return createdAt;      }
    public void setCreatedAt(LocalDateTime c){ this.createdAt = c;    }

    public LocalDateTime getUpdatedAt()      { return updatedAt;      }
    public void setUpdatedAt(LocalDateTime u){ this.updatedAt = u;    }

    @Override public String toString() { return getFullName() + " [" + empCode + "]"; }
}
