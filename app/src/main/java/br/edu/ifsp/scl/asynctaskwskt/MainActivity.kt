package br.edu.ifsp.scl.asynctaskwskt

import android.os.AsyncTask
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import br.edu.ifsp.scl.asynctaskwskt.MainActivity.constantes.URL_BASE
import kotlinx.android.synthetic.main.activity_main.*
import org.jetbrains.anko.toast
import org.json.JSONException
import org.json.JSONObject
import java.io.BufferedInputStream
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.lang.Thread.sleep
import java.net.HttpURLConnection
import java.net.URL

class MainActivity : AppCompatActivity(), View.OnClickListener {

//    val buscarInformacoesBT: Button
//    val progressBar: ProgressBar

    object constantes {
        val URL_BASE = "https://www.nobile.pro.br/sdm/"
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        // Seta o Listener para o botão usando uma função lambda
//        buscarInformacoesBt.setOnClickListener {
//            // Disparando AsyncTask para buscar texto
//            val buscarTextoAt = BuscarTextoAt()
//            buscarTextoAt.execute(URL_BASE + "texto.php")
//        }

        buscarInformacoesBt.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        val buscarDataAt = BuscarDataAt()
        buscarDataAt.execute(URL_BASE + "data.php");
    }

    // AsyncTask que fará acesso ao WebService
    private inner class BuscarDataAt : AsyncTask<String, Void, JSONObject>() {
        // Executa na mesma Thread de UI
        override fun onPreExecute() {
            super.onPreExecute()
            // Mostrando a barra de progresso
            pb_carregando.visibility = View.VISIBLE
        }

        // Executa numa outra Thread em background
        override fun doInBackground(vararg params: String?): JSONObject {
            val sb: StringBuilder = StringBuilder()
            try {
                val url = params[0]
                val conexao = URL(url).openConnection() as HttpURLConnection
                if(conexao.responseCode == HttpURLConnection.HTTP_OK) {
                    val input = conexao.inputStream
                    val br = BufferedReader(InputStreamReader(input))
                    var temp: String?
                    temp = br.readLine()
                    while(temp != null) {
                        sb.append(temp)
                        temp = br.readLine()
                    }
                }
                return JSONObject(sb.toString())
            } catch(ioex: IOException) {
                ioex.printStackTrace()
            } catch(jsone: JSONException) {
                jsone.printStackTrace()
            }
            return JSONObject()
        }

        // Executa na mesma Thread de UI
        override fun onPostExecute(s: JSONObject) {
            var data: String? = ""
            var hora: String = ""
            var ds: String? = ""

            super.onPostExecute(s)

            try {
                data = "${s.getInt("mday")}/${s.getInt("mon")}/${s.getInt("year")}"
                hora = "${s.getInt("hours")}:${s.getInt("minutes")}:${s.getInt("seconds")}"
                ds = s.getString("weekday");
            }
            catch (jsone: JSONException) {
                jsone.printStackTrace();
            }
            tv_data.setText("$data\n$hora\n$ds");
            pb_carregando.visibility = View.GONE
        }
    }

    // AsyncTask que fará acesso ao WebService
    private inner class BuscarTextoAt : AsyncTask<String, Int, String>() {
        // Executa na mesma Thread de UI
        override fun onPreExecute() {
            super.onPreExecute()
            toast("Buscando String no Web Service")
            // Mostrando a barra de progresso
            progressBar.visibility = View.VISIBLE
        }
        // Executa numa outra Thread em background
        override fun doInBackground(vararg params: String?): String {
            // Pegando URL na primeira posição do params
            val url = params[0]
            // Criando um StringBuffer para receber a resposta do Web Service
            val stringBufferResposta: StringBuffer = StringBuffer()
            try {
                // Criando uma conexão HTTP a partir da URL
                val conexao = URL(url).openConnection() as HttpURLConnection
                if (conexao.responseCode == HttpURLConnection.HTTP_OK) {
                    // Caso a conexão seja bem sucedida, resgata o InputStream da mesma
                    val inputStream = conexao.inputStream
                    // Cria um BufferedReader a partir do InputStream
                    val bufferedReader = BufferedInputStream(inputStream).bufferedReader()
                    // Lê o bufferedReader para uma lista de Strings
                    val respostaList = bufferedReader.readLines()
                    // "Appenda" cada String da lista ao StringBuffer
                    respostaList.forEach { stringBufferResposta.append(it) }
                }
            } catch (ioe: IOException) {
                toast("Erro na conexão!")
            }
            // Simulando notificação do progresso para Thread de UI
            for (i in 1..10) {
                /* Envia um inteiro para ser publicado como progresso. Esse valor é recebido pela função
                callback onProgressUpdate*/
                publishProgress(i)
                // Dormindo por 0.5 s para simular o atraso de rede
                sleep(500)
            }
            // Retorna a String formada a partir do StringBuffer
            return stringBufferResposta.toString()
        }
        // Executa na mesma Thread de UI
        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
            toast("Texto recuperado com sucesso")
            // Altera o TextView com o texto recuperado
            textoTv.text = result
            // Tornando a barra de progresso invisível
            progressBar.visibility = View.GONE
        }
        // Executa na Thread de UI, é chamado sempre após uma publishProgress
        override fun onProgressUpdate(vararg values: Int?) {
            // Se o valor de progresso não for nulo, atualiza a barra de progresso
            values[0]?.apply { progressBar.progress = this }
        }
    }
}