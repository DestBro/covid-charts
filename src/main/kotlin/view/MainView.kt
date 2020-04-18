package view

import javafx.scene.chart.CategoryAxis
import javafx.scene.chart.NumberAxis
import kotlin.math.absoluteValue
import tornadofx.*

val countries = loadCountries(online = false)

class MainView : View("COVID19 Charts") {

    override val root = scrollpane(true, true) {
        setPrefSize(900.0, 500.0)

        vbox {
            areachart("Migrants, population, cases", CategoryAxis(), NumberAxis()) {
                setMinSize(500.0, 500.0)

                series("Covid Cases") {
                    countries.sortedBy { it.covidCases }
                            .takeLast(10)
                            .forEach {
                                data(it.name, it.covidCases /** 2*/)
                            }
                }

                series("Migrants") {
                    countries.sortedBy { it.covidCases }
                            .takeLast(10)
                            .forEach {
                                data(it.name, it.migrants)
                            }
                }

                series("Population") {
                    countries.sortedBy { it.covidCases }
                            .takeLast(10)
                            .forEach {
                                data(it.name, it.population / 1000.0)
                            }
                }
            }

            bubblechart("Most effective countries by cases/deaths, extra value = population", NumberAxis(), NumberAxis()) {
                setMinSize(40.0, 700.0)

                val data = countries.sortedBy { it.covidCases.toDouble() }.takeLast(5)
                val maxP = data.maxBy { it.population }!!.population
                val percentByP = { c: Int -> (c * 100.0) / maxP }

                data.forEach {
                    val casesByPop = (it.covidCases * 100000.0) / (it.population)
                    val deathsByPop = (it.covidDeaths * 100000.0) / (it.population)

                    val x = (it.migrants.toDouble() / it.population) * 10000
                    val y = (casesByPop / deathsByPop)

                    series(it.name + ", $y") {
                        data(x, y, percentByP(it.population) / 100)
                    }
                }
            }

            piechart("Cases top 15") {
                setMinSize(900.0, 400.0)
                countries.sortedBy { it.covidCases }.takeLast(15).forEach {
                    data("${it.name} (${it.covidCases})", it.covidCases.toDouble())
                }
            }

            piechart("Deaths top 15") {
                setMinSize(900.0, 400.0)
                countries.sortedBy { it.covidDeaths }.takeLast(15).forEach {
                    data("${it.name} (${it.covidDeaths})", it.covidDeaths.toDouble())
                }
            }

            areachart("Cases top 10", CategoryAxis(), NumberAxis()) {
                setMinSize(500.0, 500.0)
                series("List of countries by deaths") {
                    countries.sortedBy { it.covidCases }.takeLast(10).forEach {
                        data(it.name, it.covidCases.toDouble())
                    }
                }
            }

            areachart("Deaths top 10", CategoryAxis(), NumberAxis()) {
                setMinSize(500.0, 500.0)
                series("List of countries by deaths") {
                    countries.sortedBy { it.covidDeaths }.takeLast(10).forEach {
                        data(it.name, it.covidDeaths.toDouble())
                    }
                }
            }

            areachart("Gravity", CategoryAxis(), NumberAxis()) {
                setMinSize(500.0, 500.0)

                val data = countries
                        .sortedBy { it.covidDeaths.toDouble() / it.population.toDouble() }
                        .takeLast(30)

                series("Cases/population") {
                    data.forEach {
                        data(it.name, it.covidCases.toDouble() / it.population.toDouble())
                    }
                }
                series("Deaths/population") {
                    data.forEach {
                        data(it.name, it.covidDeaths.toDouble() / it.population.toDouble())
                    }
                }
            }
        }
    }
}
