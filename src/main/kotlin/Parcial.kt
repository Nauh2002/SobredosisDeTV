package ar.edu.unsam.algo2

import java.time.DayOfWeek
import java.time.LocalDate
import kotlin.random.Random

class Programa {
    var titulo: String = ""
    var conductores = mutableListOf<Conductor>()
    var presupuesto = 10000
    var sponsors = mutableListOf<String>()
    lateinit var dias: DayOfWeek
    var duracion: Int = 30
    var ratings = mutableListOf<Rating>()
    val restricciones = mutableListOf<Restriccion>()

    fun promedioUltimas5Ediciones() = ratings.sortedBy { it.fecha }.takeLast(5).map { it.valor }.average()
    fun conducidoPor(nombreConductor: String) = conductores.any{conductor -> conductor.nombre == nombreConductor }
    fun mailConductores() = conductores.map{it.mail}

}

data class Conductor(var nombre: String, var mail: String)

data class Rating(var valor: Int, var fecha: LocalDate)


// Strategy
interface Restriccion {
    fun cumple(programa: Programa): Boolean
}

class PromedioMinimo(var valorMinimo: Double) : Restriccion {
    override fun cumple (programa: Programa) = programa.promedioUltimas5Ediciones() > valorMinimo
}

class ConductoresMinimo(var minimoConductores: Int): Restriccion {
    override fun cumple (programa: Programa) = programa.conductores.size >= minimoConductores
}

class ConductorEspecifico(var nombreConductor: String) : Restriccion {
    override fun cumple(programa: Programa) = programa.conducidoPor(nombreConductor)
}

class PresupuestoLimitado(var maximoPresupuesto: Int) : Restriccion {
    override fun cumple(programa: Programa) = programa.presupuesto <= maximoPresupuesto
}


class RestriccionesOrCompuestas(val restricciones: List<Restriccion>): Restriccion{
    override fun cumple(programa: Programa) = restricciones.any{it.cumple(programa)}
}

class RestriccionesAndCompuestas(val restricciones: List<Restriccion>): Restriccion{
    override fun cumple(programa: Programa) = restricciones.all{it.cumple(programa)}
}


interface AccionRevisionPrograma{
    fun ejecutar(programa: Programa, grilla: Grilla)
}


// Command
class ProgramaEn2 : AccionRevisionPrograma {
    override fun ejecutar(programa: Programa, grilla: Grilla) {
        val conductores = programa.conductores
        val medioDeConductores = conductores.size / 2

        val conductoresPrimerPrograma = conductores.take(medioDeConductores).toMutableList()
        val conductoresSegundoPrograma = conductores.drop(medioDeConductores).toMutableList()

        val presupuestoParaProgramas = programa.presupuesto / 2

        val sponsors = programa.sponsors.toMutableList()

        val mitadDeDuracion = programa.duracion / 2

        val palabras = programa.titulo.split(" ").filter { it.isNotEmpty() }

        val titulo1 = "${palabras[0]} en el aire!"
        val titulo2 = if (palabras.size > 1) palabras[1].replaceFirstChar { it.uppercase() } else "Programa sin nombre"

        val dias = programa.dias


        grilla.agregarPrograma(
            ProgramFactory.crear(
                conductoresSegundoPrograma,
                presupuestoParaProgramas,
                sponsors,
                mitadDeDuracion,
                titulo2,
                dias
            )
        )

        grilla.agregarPrograma(
            ProgramFactory.crear(
                conductoresPrimerPrograma,
                presupuestoParaProgramas,
                sponsors,
                mitadDeDuracion,
                titulo1,
                dias
            )
        )
    }
}

object ProgramFactory {
    fun crear(
        conductoresNuevo: MutableList<Conductor>,
        presupuestoNuevo: Int,
        sponsorsNuevo: MutableList<String>,
        duracionNuevo: Int,
        tituloNuevo: String,
        diasNuevo: DayOfWeek): Programa{
        return Programa().apply {
            titulo = tituloNuevo
            conductores = conductoresNuevo
            presupuesto = presupuestoNuevo
            sponsors = sponsorsNuevo
            duracion = duracionNuevo
            dias = diasNuevo

        }
    }
}

object SimpsoncFactory{
    fun crear(programa: Programa): Programa{
        return Programa().apply{
            titulo = "Los Simpson"
            dias = programa.dias
            duracion = programa.duracion
        }
    }
}

class CambiarPorLosSimpson : AccionRevisionPrograma{
    override fun ejecutar(programa: Programa, grilla: Grilla) {
        val programaReemplazador = SimpsoncFactory.crear(programa)

        grilla.removerPrograma(programa)
        grilla.agregarPrograma(programaReemplazador)
    }
}

class FusionDeProgramas : AccionRevisionPrograma{
    override fun ejecutar(programa: Programa, grilla: Grilla) {
        val programas = grilla.programas

        val indiceDePrograma1 = programas.indexOf(programa)

        val programa1 = programa
        val programa2 = if (indiceDePrograma1 != programas.lastIndex) {
            programas[indiceDePrograma1 + 1]
        } else {
            programas[0]
        }

        val conductores = mutableListOf(
            programa1.conductores.first(),
            programa2.conductores.first()
        )

        val presupuesto = minOf(programa1.presupuesto, programa2.presupuesto)

        val sponsors = if (Random.nextBoolean()) programa1.sponsors else programa2.sponsors

        val duracion = programa1.duracion + programa2.duracion

        val dias = programa1.dias

        val titulo = if (Random.nextBoolean()) "Impacto Total" else "Buen Dia"


        programas.remove(programa1)
        programas.remove(programa2)

        grilla.agregarPrograma( ProgramFactory.crear(
                conductores,
                presupuesto,
                sponsors,
                duracion,
                titulo,
                dias
        )
        )
    }
}

class CambioDeDia(val nuevoDia: DayOfWeek): AccionRevisionPrograma{
    override fun ejecutar(programa: Programa, grilla: Grilla) {
        programa.dias = nuevoDia
    }
}

class Grilla {
    val programas = mutableListOf<Programa>()

    fun agregarPrograma(programa: Programa) {
        programas.add(programa)
    }

    fun removerPrograma(programa: Programa) {
        programas.remove(programa)
    }
}




