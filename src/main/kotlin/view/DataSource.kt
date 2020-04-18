package view

import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import tech.tablesaw.api.IntColumn
import tech.tablesaw.api.Row
import tech.tablesaw.api.StringColumn
import tech.tablesaw.api.Table
import tech.tablesaw.io.Destination
import java.io.File

const val COVID_CASES_URL = "https://www.ecdc.europa.eu/en/geographical-distribution-2019-ncov-cases"
const val POPULATION_DATA_CSV_FILE_PATH = "src/main/resources/population_by_country_2020.csv"
const val COVID_CASES_CSV_FILE_PATH = "src/main/resources/covidData.csv"

fun loadCountries(online: Boolean = false) = if (online) {
    fetchCovidCases()
} else {
    Table.read().csv(COVID_CASES_CSV_FILE_PATH)
}
        .joinOn("name")
        .inner(Table.read().csv(POPULATION_DATA_CSV_FILE_PATH), POPULATION_COLUMNS[0])
        .map { rowToAdvancedCountry(it) }

private fun fetchCovidCases() = Jsoup
        .connect(COVID_CASES_URL)
        .get()
        .select(".table > tbody:nth-child(2) > tr > td:nth-child(2)")
        .map { asCountry(it) }
        .filter { it.name.isNotBlank() }
        .toTable()

private fun asCountry(it: Element) = Country(
        cleanName(it.text()),
        it.nextElementSibling().text().toInt(),
        it.nextElementSibling().nextElementSibling().text().toInt())

data class Country(val name: String, val cases: Int, val deaths: Int)

data class AdvancedCountry(val name: String,
                           val covidCases: Int,
                           val covidDeaths: Int,
                           val population: Int,
                           val yearlyChange: String,
                           val netChange: Int,
                           val density: Int,
                           val area: Int,
                           val migrants: Int,
                           val fertRate: Double,
                           val medAge: Int,
                           val urbanPop: String,
                           val worldShare: String)

fun List<Country>.toTable() = Table.create(
        StringColumn.create("name", map { it.name }),
        IntColumn.create("cases", map { it.cases }.toTypedArray()),
        IntColumn.create("deaths", map { it.deaths }.toTypedArray()))
        .also { //Cache covid data for offline use
            Table.defaultWriterRegistry
                    .getWriterForExtension("csv")
                    .write(it, Destination(File("src/main/resources/covidData.csv")))
        }

fun rowToAdvancedCountry(row: Row) = AdvancedCountry(
        row.getString("name"),
        row.getInt("cases"),
        row.getInt("deaths"),
        row.getInt("Population (2020)"),
        row.getString("Yearly Change"),
        row.getInt("Net Change"),
        row.getInt("Density (P/Km²)"),
        row.getInt("Land Area (Km²)"),
        row.getInt("Migrants (net)"),
        row.getString("Fert. Rate").toDoubleOrNull() ?: 0.0,
        row.getString("Med. Age").toIntOrNull() ?: 0,
        row.getString("Urban Pop %"),
        row.getString("World Share")
)

val exceptions = mapOf(
        "United States of America" to "United States",
        "Cape Verde" to "Cabo Verde",
        "Cote dIvoire" to "Côte d'Ivoire",
        "Democratic Republic of the Congo" to "DR Congo",
        "Guinea Bissau" to "Guinea-Bissau",
        "Sao Tome and Principe" to "Sao Tome & Principe",
        "United Republic of Tanzania" to "Tanzania",
        "Bonaire, Saint Eustatius and Saba" to "Caribbean Netherlands",
        "Falkland Islands (Malvinas)" to "Falkland Islands",
        "Saint Kitts and Nevis" to "Saint Kitts & Nevis",
        "Saint Vincent and the Grenadines" to "St. Vincent & Grenadines",
        "Turks and Caicos islands" to "Turks and Caicos",
        "United States Virgin Islands" to "U.S. Virgin Islands",
        "Brunei Darussalam" to "Brunei",
        "Palestine" to "State of Palestine",
        "Timor Leste" to "Timor-Leste",
        "Czechia" to "Czech Republic (Czechia)",
        "Faroe Islands" to "Faeroe Islands",
        "Guernsey" to "Channel Islands",
        "Jersey" to "Channel Islands",
        "Palestine" to "State of Palestine",
        "Palestine" to "State of Palestine",
        "Palestine" to "State of Palestine",
        "Cote dIvoire" to "Côte d'Ivoire"
)

fun cleanName(name: String): String {
    val newName = name.replace("_", " ")
    return if (exceptions.containsKey(newName)) {
        exceptions.getOrElse(newName) { newName }
    } else {
        newName
    }
}

val POPULATION_COLUMNS = listOf(
        "Country (or dependency)",
        "Population (2020)",
        "Yearly Change",
        "Net Change",
        "Density (P/Km²)",
        "Land Area (Km²)",
        "Migrants (net)",
        "Fert. Rate",
        "Med. Age",
        "Urban Pop %",
        "World Share")