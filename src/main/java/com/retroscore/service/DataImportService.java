package com.retroscore.service;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;
import com.retroscore.entity.FootballClub;
import com.retroscore.entity.Match;
import com.retroscore.entity.Season;
import com.retroscore.repository.FootballClubRepository;
import com.retroscore.repository.MatchRepository;
import com.retroscore.repository.SeasonRepository;
import jakarta.transaction.Transactional;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class DataImportService {

    private final MatchRepository matchRepository;
    private final SeasonRepository seasonRepository;
    private final FootballClubRepository footballClubRepository;


    private  static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @Transactional
    public void importSeasonData(String csvFilePath, String seasonName) {
        log.info("Starting import for season: {} from file {}", seasonName, csvFilePath);

        try {
            List<String[]> csvData = parseCsvFile(csvFilePath);

            Season season = findOrCreateSeason(seasonName);

            int successCount = 0;
            int errorCount = 0;

            for (int i = 1; i <csvData.size();i++){
                String[] row = csvData.get(i);
                try{
                    if (isValidRow(row)){
                        Match match = createMatchFromRow(row,season);
                        matchRepository.save(match);
                        successCount++;
                        log.debug("Successfully imported a match:{} vs {}",
                                match.getHomeTeam().getName(), match.getAwayTeam().getName());
                    }
                } catch (Exception e){
                    errorCount++;
                    log.error("Error processing row {} : {}", i +1, e.getMessage());
                }
            }

            log.info("Import completed for season: {}. Success:{}, Errors:{}",
                    seasonName, successCount, errorCount);

        } catch (Exception e) {
           log.error("Failed to import season data :{}", e.getMessage(),e);
           throw new RuntimeException("Failed to import season data",e);

        }
    }

        public List<String[]> parseCsvFile(String csvFilePath) throws IOException, CsvException{
            try (CSVReader reader = new CSVReader(new FileReader(csvFilePath))) {
                    return reader.readAll();
            }
    }

    private Season findOrCreateSeason(String seasonName){
        return seasonRepository.findBySeasonName(seasonName)
                .orElseGet(() -> {
                    Season newSeason = new Season();
                    newSeason.setSeasonName(seasonName);
                    return seasonRepository.save(newSeason);
                });
    }

    private boolean isValidRow(String[] row){
        return row.length >= 23 &&
                StringUtils.hasText(row[1]) &&
                StringUtils.hasText(row[3]) &&
                StringUtils.hasText(row[4]) &&
                StringUtils.hasText(row[5]) &&
                StringUtils.hasText(row[6]);
    }

    private Match createMatchFromRow(String[] row, Season season){
        Match match = new Match();

        match.setSeason(season);

        match.setMatchDate(parseDate(row[1]));

        match.setHomeTeam(findOrCreateTeam(row[3]));
        match.setAwayTeam(findOrCreateTeam(row[4]));

        match.setHomeScore(parseInteger(row[5]));
        match.setAwayScore(parseInteger(row[6]));

        match.setHalftimeHomeScore(parseInteger(row[8]));
        match.setHalftimeAwayScore(parseInteger(row[9]));

        if(row.length >18) match.setHomeCorners(parseInteger(row[18]));
        if(row.length >19) match.setAwayCorners(parseInteger(row[19]));
        if(row.length >20) match.setHomeYellowCards(parseInteger(row[20]));
        if(row.length >21) match.setAwayYellowCards(parseInteger(row[21]));
        if(row.length >22) match.setHomeRedCards(parseInteger(row[22]));
        if(row.length >23) match.setAwayRedCards(parseInteger(row[23]));

        return match;
    }

    private FootballClub findOrCreateTeam(String teamName){
        return footballClubRepository.findByName(teamName)
                .orElseGet(()-> {
                    FootballClub newClub = new FootballClub();
                    newClub.setName(teamName);
                    return footballClubRepository.save(newClub);
                });
    }

    private LocalDate parseDate(String dateStr){
        try {
            return LocalDate.parse(dateStr, DATE_FORMATTER);
        } catch (DateTimeParseException e){
            log.error("Failed to parse date:{}", dateStr);
            throw new RuntimeException("Invalid date format: "+ dateStr, e);
        }
    }

    private Integer parseInteger(String value){
        if(!StringUtils.hasText(value)){
            return null;
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e){
            log.warn("Failed to parse integer:{}", value);
            return null;
        }
    }

    public ImportStatistics getImportStatistics(String csvFilePath){
        try{
            List<String[]> csvData = parseCsvFile(csvFilePath);
            int totalRows = csvData.size();
            int validRows = 0;
            int invalidRows = 0;

            for (int i = 1; i< csvData.size(); i ++){
                if(isValidRow(csvData.get(i))){
                    validRows++;
                } else{
                    invalidRows++;
                }
            }
            return new ImportStatistics(totalRows,validRows,invalidRows);
        } catch (Exception e){
            log.error("Failed to get import statistics:{}", e.getMessage());
            throw new RuntimeException("Failed to get import statistics", e);
        }
    }

    @Getter
    public static class ImportStatistics {
        private final int totalRows;
        private final int validRows;
        private final int invalidRows;

        public ImportStatistics(int totalRows, int validRows,int invalidRows){
            this.totalRows = totalRows;
            this.validRows = validRows;
            this.invalidRows = invalidRows;

        }

        @Override
        public String toString(){
            return String.format("ImportStatistics{total=%d, valid=%d,invalid=%d}", totalRows,validRows,invalidRows);
        }
    }



}
