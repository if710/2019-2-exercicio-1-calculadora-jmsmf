package br.ufpe.cin.android.calculadora

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    var info: String = ""
    var expression: String = ""

    // Needed to keep the state of important variables
    companion object {
        val CURR_INFO = "currInfo"
        val CURR_EXPRESSION = "currExpression"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // make an array of all buttons, keeps code concise
        val buttonsArray = arrayListOf<View>(btn_0, btn_1, btn_2, btn_3, btn_4, btn_5, btn_6, btn_7,
            btn_8, btn_9, btn_Dot, btn_Divide, btn_Multiply, btn_Subtract, btn_Add, btn_Power,
            btn_LParen, btn_RParen)

        // adds generic listener for expression characters
        for (button in buttonsArray) {
            button.setOnClickListener { charPressed(it) }
        }

        btn_Equal.setOnClickListener { giveResult() } // gives the result to the expression written
        btn_Clear.setOnClickListener { removeLast() } // removes last character in the expression
        btn_Clear.setOnLongClickListener { clearAll() } //completely empties the expression
    }

    // Saves the state of current relevant information
    override fun onSaveInstanceState(outState: Bundle?) {

        outState?.run {
            putString(CURR_INFO, info)
            putString(CURR_EXPRESSION, expression)
        }

        super.onSaveInstanceState(outState)
    }

    // Loads the state of current relevant information
    override fun onRestoreInstanceState(savedInstanceState: Bundle?) {
        super.onRestoreInstanceState(savedInstanceState)

        savedInstanceState?.run {
            info = getString(CURR_INFO).toString()
            expression = getString(CURR_EXPRESSION).toString()

            text_info.setText(info)
            text_calc.setText(expression)
        }
    }

    // Function that gets the char associated with the pressed button and adds it to the expression
    fun charPressed(button: View){
        if (button is Button){
            val character = button.text // Gets character
            expression += character // Puts it at the end of the expression
            text_calc.setText(expression)
        }
    }

    // tries to perform the eval function, if unsuccessful display error message
    fun giveResult() {
        info = expression
        try {

            expression = eval(expression).toString()
            text_info.setText(info)
            text_calc.setText(expression)

        } catch(error: RuntimeException) {

            if (error.message is String){
                Toast.makeText(applicationContext, error.message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    // removes the last character in the expression, similar to a backspace key
    fun removeLast() {
        val length = expression.length
        if (length > 0){
            expression = expression.substring(startIndex = 0, endIndex = length-1)
            text_calc.setText(expression)
        }
    }

    // empties the expression, as the name implies
    fun clearAll(): Boolean {
        expression = ""
        text_calc.setText(expression)
        return true
    }

    //Como usar a função:
    // eval("2+2") == 4.0
    // eval("2+3*4") = 14.0
    // eval("(2+3)*4") = 20.0
    //Fonte: https://stackoverflow.com/a/26227947
    fun eval(str: String): Double {
        return object : Any() {
            var pos = -1
            var ch: Char = ' '
            fun nextChar() {
                val size = str.length
                ch = if ((++pos < size)) str.get(pos) else (-1).toChar()
            }

            fun eat(charToEat: Char): Boolean {
                while (ch == ' ') nextChar()
                if (ch == charToEat) {
                    nextChar()
                    return true
                }
                return false
            }

            fun parse(): Double {
                nextChar()
                val x = parseExpression()
                if (pos < str.length) throw RuntimeException("Caractere inesperado: " + ch)
                return x
            }

            // Grammar:
            // expression = term | expression `+` term | expression `-` term
            // term = factor | term `*` factor | term `/` factor
            // factor = `+` factor | `-` factor | `(` expression `)`
            // | number | functionName factor | factor `^` factor
            fun parseExpression(): Double {
                var x = parseTerm()
                while (true) {
                    if (eat('+'))
                        x += parseTerm() // adição
                    else if (eat('-'))
                        x -= parseTerm() // subtração
                    else
                        return x
                }
            }

            fun parseTerm(): Double {
                var x = parseFactor()
                while (true) {
                    if (eat('*'))
                        x *= parseFactor() // multiplicação
                    else if (eat('/'))
                        x /= parseFactor() // divisão
                    else
                        return x
                }
            }

            fun parseFactor(): Double {
                if (eat('+')) return parseFactor() // + unário
                if (eat('-')) return -parseFactor() // - unário
                var x: Double
                val startPos = this.pos
                if (eat('(')) { // parênteses
                    x = parseExpression()
                    eat(')')
                } else if ((ch in '0'..'9') || ch == '.') { // números
                    while ((ch in '0'..'9') || ch == '.') nextChar()
                    x = java.lang.Double.parseDouble(str.substring(startPos, this.pos))
                } else if (ch in 'a'..'z') { // funções
                    while (ch in 'a'..'z') nextChar()
                    val func = str.substring(startPos, this.pos)
                    x = parseFactor()
                    if (func == "sqrt")
                        x = Math.sqrt(x)
                    else if (func == "sin")
                        x = Math.sin(Math.toRadians(x))
                    else if (func == "cos")
                        x = Math.cos(Math.toRadians(x))
                    else if (func == "tan")
                        x = Math.tan(Math.toRadians(x))
                    else
                        throw RuntimeException("Função desconhecida: " + func)
                } else {
                    throw RuntimeException("Caractere inesperado: " + ch.toChar())
                }
                if (eat('^')) x = Math.pow(x, parseFactor()) // potência
                return x
            }
        }.parse()
    }
}
