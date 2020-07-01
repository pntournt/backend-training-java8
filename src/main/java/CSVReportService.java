import model.Transaction;
import repositories.TransactionRepository;

import javax.management.relation.Role;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CSVReportService {

    private final PersonsService personsService;
    private final TransactionRepository transactionRepository;

    public CSVReportService(PersonsService personsService, TransactionRepository transactionRepository) {
        this.personsService = personsService;
        this.transactionRepository = transactionRepository;
    }

    /**
     * Retrieve the average consumption (transaction amount) per @{@link model.Person}'s distinct roles during the last month
     *
     * Note that roles are just tags that each person is assigned. ie 'student', 'gamer', 'athlete', 'parent'
     * a Person may have multiple roles or none.
     *
     * @return data in csv file format,
     *         where the first line depict the roles
     *         and the second line the average consumption per role
     * ie: (formatted example -- the actual output should be just comma separated)
     * |student, gamer, parent|
     * |10.50  , 20.10, 0     |
     */
    public String getAverageConsumptionPerRoleDuringTheLastMonth() {
        List<Transaction> transactionList = this.transactionRepository.getTransactions(LocalDateTime.now().minusMonths(1));
        List<String> roles = transactionList.stream()
                .map(tr -> this.personsService.getPersonByEmailAddress(tr.getEmailAddress()))
                .map(p -> p.get().getRoles())
                .flatMap(Collection::stream)
                .distinct()
                .collect(Collectors.toList());

        Map<String, OptionalDouble> rolesAVG = roles.stream()
                .map(role -> new AbstractMap.SimpleEntry<String, OptionalDouble>
                        (role, transactionList.stream()
                                .filter(
                                        tr -> this.personsService.getPersonByEmailAddress(tr.getEmailAddress()).get().getRoles().contains(role)
                                ).mapToDouble(Transaction::getAmount).average()
                        ))
        .collect(Collectors.toMap(entry-> entry.getKey(), entry->entry.getValue()));

        String csv = "";

        for (String r : rolesAVG.keySet()) {
            csv += r + ",";
        }
        csv += "\n";

        for (OptionalDouble v : rolesAVG.values()) {
            csv += v.getAsDouble() + ",";
        }

        return csv;

        //throw new UnsupportedOperationException();
    }
}
