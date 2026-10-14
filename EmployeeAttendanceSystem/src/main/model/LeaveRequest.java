package main.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Represents an employee's leave request.
 */
public class LeaveRequest {

    public enum Status { Pending, Approved, Rejected, Cancelled }

    private int           id;
    private int           employeeId;
    private String        employeeName;   // joined
    private String        empCode;        // joined
    private int           leaveTypeId;
    private String        leaveTypeName;  // joined
    private LocalDate     fromDate;
    private LocalDate     toDate;
    private String        reason;
    private Status        status;
    private LocalDateTime appliedOn;
    private Integer       reviewedBy;
    private LocalDateTime reviewedOn;
    private String        reviewRemarks;

    public LeaveRequest() {}

    // ── Getters / Setters ───────────────────────────────────────────────────
    public int    getId()                    { return id;            }
    public void   setId(int id)              { this.id = id;         }

    public int    getEmployeeId()            { return employeeId;    }
    public void   setEmployeeId(int e)       { this.employeeId = e;  }

    public String getEmployeeName()          { return employeeName;  }
    public void   setEmployeeName(String n)  { this.employeeName=n;  }

    public String getEmpCode()               { return empCode;       }
    public void   setEmpCode(String c)       { this.empCode = c;     }

    public int    getLeaveTypeId()           { return leaveTypeId;   }
    public void   setLeaveTypeId(int l)      { this.leaveTypeId = l; }

    public String getLeaveTypeName()         { return leaveTypeName; }
    public void   setLeaveTypeName(String n) { this.leaveTypeName=n; }

    public LocalDate getFromDate()           { return fromDate;      }
    public void      setFromDate(LocalDate d){ this.fromDate = d;    }

    public LocalDate getToDate()             { return toDate;        }
    public void      setToDate(LocalDate d)  { this.toDate = d;      }

    public String getReason()                { return reason;        }
    public void   setReason(String r)        { this.reason = r;      }

    public Status getStatus()                { return status;        }
    public void   setStatus(Status s)        { this.status = s;      }

    public LocalDateTime getAppliedOn()          { return appliedOn;  }
    public void setAppliedOn(LocalDateTime a)     { this.appliedOn=a;  }

    public Integer getReviewedBy()               { return reviewedBy;  }
    public void    setReviewedBy(Integer r)      { this.reviewedBy=r;  }

    public LocalDateTime getReviewedOn()         { return reviewedOn;  }
    public void          setReviewedOn(LocalDateTime r){ this.reviewedOn=r;}

    public String getReviewRemarks()             { return reviewRemarks;}
    public void   setReviewRemarks(String r)     { this.reviewRemarks=r;}
}
