package midianet.trip.bot.user.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.lang.NonNull;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Past;
import javax.validation.constraints.Size;
import java.time.LocalDate;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {
    @Id
    private Long id;

    @NotBlank
    @Size(max = 80)
    @Column(nullable = false, length = 80)
    private String name;

    @NotNull
    @Column(nullable = false, length = 10)
    private Status status;

//    @Past
//    private LocalDate nascimento;
//
//    @Size(max = 10)
//    @Column(length = 10)
//    private String rg;
//
//    @ManyToOne
//    private Familia familia;

    public enum Status{
        INTERESTED,ASSOCIATE,CONFIRMED;

    }

}
