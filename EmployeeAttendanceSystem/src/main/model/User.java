package main.model;

import java.time.LocalDateTime;

/**
 * Represents an application user who can log in.
 */
public class User {

    public enum Role { ADMIN, MANAGER, USER }

    private int           id;
    private String        username;
    private String        password;     // SHA-256 hex
    private Role          role;
    private String        fullName;
    private String        email;
    private boolean       active;
    private LocalDateTime createdAt;

    public User() {}

    public User(int id, String username, String password,
                Role role, String fullName, String email, boolean active) {
        this.id       = id;
        this.username = username;
        this.password = password;
        this.role     = role;
        this.fullName = fullName;
        this.email    = email;
        this.active   = active;
    }

    // ── Getters / Setters ───────────────────────────────────────────────────
    public int           getId()        { return id;        }
    public void          setId(int id)  { this.id = id;     }

    public String        getUsername()               { return username;          }
    public void          setUsername(String username){ this.username = username; }

    public String        getPassword()               { return password;          }
    public void          setPassword(String password){ this.password = password; }

    public Role          getRole()                   { return role;              }
    public void          setRole(Role role)           { this.role = role;         }

    public String        getFullName()               { return fullName;          }
    public void          setFullName(String fn)      { this.fullName = fn;       }

    public String        getEmail()                  { return email;             }
    public void          setEmail(String email)      { this.email = email;       }

    public boolean       isActive()                  { return active;            }
    public void          setActive(boolean a)        { this.active = a;          }

    public LocalDateTime getCreatedAt()              { return createdAt;         }
    public void          setCreatedAt(LocalDateTime c){ this.createdAt = c;      }

    @Override public String toString() { return fullName + " (" + username + ")"; }
}
