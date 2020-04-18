package view

import javafx.scene.chart.CategoryAxis
import javafx.scene.chart.NumberAxis
import tornadofx.*

val countries = loadCountries(fetch = false)

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
                                data(it.name, it.covidCases
                                        /** 2*/)
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

            bubblechart("X = population %; Y = cases/deaths; Extra value = deaths/population", NumberAxis(), NumberAxis()) {
                setMinSize(100.0, 500.0)

                val data = countries.sortedBy { it.covidCases.toDouble() }.takeLast(5)
                val maxP = data.maxBy { it.population }!!.population
                val percentByP = { c: Int -> (c * 100.0) / maxP }

                data.forEach {
                    val x = percentByP(it.population)
                    val y = it.covidCases.toDouble() / it.covidDeaths.toDouble()
                    val extra = (it.covidDeaths.toDouble() / it.population) * 5000

                    series(it.name + "(y=$y)") {
                        data(x, y, extra)
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
                series("Cases") {
                    countries.sortedBy { it.covidCases }.takeLast(10).forEach {
                        data(it.name, it.covidCases.toDouble())
                    }
                }
            }

            areachart("Deaths top 10", CategoryAxis(), NumberAxis()) {
                setMinSize(500.0, 500.0)
                series("Deaths") {
                    countries.sortedBy { it.covidDeaths }.takeLast(10).forEach {
                        data(it.name, it.covidDeaths.toDouble())
                    }
                }
            }

            areachart("Gravity", CategoryAxis(), NumberAxis()) {
                setMinSize(500.0, 500.0)

                val data = countries
                        .sortedBy { it.covidCases.toDouble() / it.population.toDouble() }
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
