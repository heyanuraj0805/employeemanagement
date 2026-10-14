package main.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Represents one attendance record for an employee on a given date.
 */
public class Attendance {

    public enum Status {
        Present, Absent, Late, HalfDay, OnLeave, Holiday, Weekend;

        /** DB-safe string (e.g. HalfDay → "Half-Day"). */
        public String toDbString() {
            return switch (this) {
                case HalfDay  -> "Half-Day";
                case OnLeave  -> "On-Leave";
                default       -> this.name();
            };
        }

        public static Status fromDbString(String s) {
            return switch (s) {
                case "Half-Day" -> HalfDay;
                case "On-Leave" -> OnLeave;
                default         -> Status.valueOf(s);
            };
        }
    }

    private int           id;
    private int           employeeId;
    private String        employeeName;     // joined
    private String        empCode;          // joined
    private String        departmentName;   // joined
    private LocalDate     attendanceDate;
    private LocalDateTime checkIn;
    private LocalDateTime checkOut;
    private Status        status;
    private double        workingHours;
    private double        overtimeHours;
    private String        remarks;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Attendance() {}

    // ── Getters / Setters ───────────────────────────────────────────────────
    public int    getId()                { return id;              }
    public void   setId(int id)          { this.id = id;           }

    public int    getEmployeeId()        { return employeeId;      }
    public void   setEmployeeId(int e)   { this.employeeId = e;    }

    public String getEmployeeName()      { return employeeName;    }
    public void   setEmployeeName(String n){ this.employeeName=n;  }

    public String getEmpCode()           { return empCode;         }
    public void   setEmpCode(String c)   { this.empCode = c;       }

    public String getDepartmentName()        { return departmentName;    }
    public void   setDepartmentName(String n){ this.departmentName = n;  }

    public LocalDate     getAttendanceDate()          { return attendanceDate;   }
    public void          setAttendanceDate(LocalDate d){ this.attendanceDate = d;}

    public LocalDateTime getCheckIn()              { return checkIn;          }
    public void          setCheckIn(LocalDateTime c){ this.checkIn = c;       }

    public LocalDateTime getCheckOut()              { return checkOut;         }
    public void          setCheckOut(LocalDateTime c){ this.checkOut = c;      }

    public Status getStatus()            { return status;          }
    public void   setStatus(Status s)    { this.status = s;        }

    public double getWorkingHours()      { return workingHours;    }
    public void   setWorkingHours(double h){ this.workingHours=h;  }

    public double getOvertimeHours()     { return overtimeHours;   }
    public void   setOvertimeHours(double h){ this.overtimeHours=h;}

    public String getRemarks()           { return remarks;         }
    public void   setRemarks(String r)   { this.remarks = r;       }

    public LocalDateTime getCreatedAt()      { return createdAt;    }
    public void setCreatedAt(LocalDateTime c){ this.createdAt = c;  }

    public LocalDateTime getUpdatedAt()      { return updatedAt;    }
    public void setUpdatedAt(LocalDateTime u){ this.updatedAt = u;  }
}
