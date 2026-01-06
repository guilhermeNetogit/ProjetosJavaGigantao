import br.com.sankhya.extensions.actionbutton.AcaoRotinaJava;
import br.com.sankhya.extensions.actionbutton.ContextoAcao;
import br.com.sankhya.modelcore.util.PesquisaCepHelper;
import br.com.sankhya.modelcore.util.PesquisaCepHelper.Endereco;
import java.util.List;

public class ConsultaCep implements AcaoRotinaJava {//@guilhermeNetoGig

    @Override
    public void doAction(ContextoAcao contexto) throws Exception {
        String cep = (String) contexto.getParam("CEP");
        if (cep == null || cep.trim().isEmpty()) {
            contexto.setMensagemRetorno("Parâmetro CEP não informado.");
            return;
        }

        // 1. Base local
        List<Endereco> ceps = PesquisaCepHelper.obterDadosDoCepLocal(cep);
        if (!ceps.isEmpty()) {
            contexto.setMensagemRetorno(buildResponse(ceps, "Base de Dados Local"));
            return;
        }

        // 2. Tentativas externas
        String[] provedores = {"Correios", "República Virtual", "ViaCEP"};
        for (String prov : provedores) {
            try {
                switch (prov) {
                    case "Correios":
                        ceps = PesquisaCepHelper.obterDadosDoCepCorreios(cep, false);
                        break;
                    case "República Virtual":
                        ceps = PesquisaCepHelper.obterDadosDoCepRepublicaVirtual(cep, false);
                        break;
                    case "ViaCEP":
                        ceps = PesquisaCepHelper.obterDadosDoCepViaCep(cep, false);
                        break;
                }

                if (!ceps.isEmpty()) {
                    contexto.setMensagemRetorno(buildResponse(ceps, prov));
                    return;
                }
            } catch (Exception e) {
                // Log opcional (se houver logger)
                // System.out.println("Falha no provedor " + prov + ": " + e.getMessage());
                // Continua para o próximo provedor
            }
        }

        // Nenhuma fonte retornou resultado
        contexto.setMensagemRetorno("O CEP não foi encontrado em nenhuma API conhecida ou não existe!");
    }

    private String buildResponse(List<Endereco> ceps, String provedor) {
        StringBuilder ret = new StringBuilder();
        ret.append("<html>");
        ret.append("<b>Provedor: </b>").append(provedor).append("<br/>");

        for (Endereco endereco : ceps) {
            ret.append("<b>Endereço </b><br/>");
            ret.append("<b>CEP:</b> ").append(endereco.getCep()).append("<br/>");
            ret.append("<b>TipoEnd:</b> ").append(endereco.getTipoEndereco()).append("<br/>");
            ret.append("<b>codEnd:</b> ").append(endereco.getCodEnd()).append("<br/>");
            ret.append("<b>DescEndereço:</b> ").append(endereco.getDescEnd()).append("<br/>");
            ret.append("<b>codBairro:</b> ").append(endereco.getCodBairro()).append("<br/>");
            ret.append("<b>DescBairro:</b> ").append(endereco.getDescBairro()).append("<br/>");
            ret.append("<b>codCidade:</b> ").append(endereco.getCodCid()).append("<br/>");
            ret.append("<b>DescCidade:</b> ").append(endereco.getDescCid()).append("<br/>");
            ret.append("<b>codUf:</b> ").append(endereco.getCodUf()).append("<br/>");
            ret.append("<b>DescUF:</b> ").append(endereco.getDescUf()).append("<br/>");
            ret.append("<br/>");
        }
        ret.append("</html>");
        return ret.toString();
    }

}
