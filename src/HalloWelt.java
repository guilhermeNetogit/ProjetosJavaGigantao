import br.com.sankhya.extensions.actionbutton.AcaoRotinaJava;
import br.com.sankhya.extensions.actionbutton.ContextoAcao;
import java.text.SimpleDateFormat;
import java.util.Date;

public class HalloWelt implements AcaoRotinaJava {// Github <- Eclipse @guilhermeNetogit 06/01/2026 19:41; Duda passou aqui 05/01/26 20:12

    @Override
    public void doAction(ContextoAcao ctx) throws Exception {
    	String name = (String) ctx.getParam("NAME");
        // Simulando entrada de dados (você pode pegar isso de variáveis de contexto se quiser)
        ctx.getParam("NAME"); 
        Date hoje = new Date();

        // Formata a data
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        String dataFormatada = sdf.format(hoje);

        // Exibe mensagem para o usuário via popup no Sankhya
        String mensagem = String.format(
            "<b>Testando Github <- Eclipse!!</b>\n\n <b>Hallo Welt!</b>\nIhr Java-Code hat funktioniert.\nWie geht’s %s?\nHeute ist %s.",
            name, dataFormatada
        );

        // Mostra no Sankhya
        ctx.setMensagemRetorno(mensagem);
    }
}






