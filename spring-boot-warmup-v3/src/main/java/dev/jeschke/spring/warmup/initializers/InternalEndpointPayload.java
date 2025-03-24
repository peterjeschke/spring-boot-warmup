package dev.jeschke.spring.warmup.initializers;

import jakarta.validation.constraints.AssertFalse;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Negative;
import jakarta.validation.constraints.NegativeOrZero;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.OffsetDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InternalEndpointPayload {
    public static InternalEndpointPayload createDefault() {
        return InternalEndpointPayload.builder()
                .notEmpty("notEmpty")
                .notBlank("notBlank")
                .number(BigInteger.TEN)
                .decimal(BigDecimal.TEN)
                .negative(-10)
                .negativeOrZero(0)
                .positive(10)
                .positiveOrZero(0)
                .alwaysTrue(true)
                .alwaysFalse(false)
                .digits(BigDecimal.TEN)
                .email("foo@bar.com")
                .future(OffsetDateTime.MAX)
                .futureOrPresent(OffsetDateTime.MAX)
                .past(OffsetDateTime.MIN)
                .pastOrPresent(OffsetDateTime.MIN)
                .pattern("abc")
                .size(List.of("a", "b"))
                .build();
    }

    @NotEmpty
    private String notEmpty;

    @NotBlank
    private String notBlank;

    @Min(1)
    @Max(15)
    @NotNull
    private BigInteger number;

    @DecimalMin("1.5")
    @DecimalMax("10.5")
    private BigDecimal decimal;

    @Negative
    private int negative;

    @NegativeOrZero
    private int negativeOrZero;

    @Positive
    private int positive;

    @PositiveOrZero
    private int positiveOrZero;

    @AssertTrue
    private boolean alwaysTrue;

    @AssertFalse
    private boolean alwaysFalse;

    @Digits(integer = 3, fraction = 2)
    private BigDecimal digits;

    @Email
    private String email;

    @Future
    private OffsetDateTime future;

    @FutureOrPresent
    private OffsetDateTime futureOrPresent;

    @Past
    private OffsetDateTime past;

    @PastOrPresent
    private OffsetDateTime pastOrPresent;

    @Pattern(regexp = "^abc+$")
    private String pattern;

    @Size(min = 1, max = 10)
    private List<String> size;
}
