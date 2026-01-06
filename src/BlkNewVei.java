import br.com.sankhya.extensions.eventoprogramavel.EventoProgramavelJava;
import br.com.sankhya.jape.event.PersistenceEvent;
import br.com.sankhya.jape.event.TransactionContext;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.modelcore.MGEModelException;
import java.math.BigDecimal;

public class BlkNewVei implements EventoProgramavelJava {
	
	private static final BigDecimal ANO_MINIMO = new BigDecimal("2021");
	
	@Override
   public void afterDelete(PersistenceEvent arg0) throws Exception {
   }

	@Override
   public void afterInsert(PersistenceEvent arg0) throws Exception {
   }

	@Override
   public void afterUpdate(PersistenceEvent arg0) throws Exception {
   }

	@Override
   public void beforeCommit(TransactionContext arg0) throws Exception {
   }

	@Override
   public void beforeDelete(PersistenceEvent arg0) throws Exception {
   }
	@Override
   public void beforeInsert(PersistenceEvent arg0) throws Exception {
		DynamicVO vo = (DynamicVO) arg0.getVo();
        BigDecimal anoFabric = vo.asBigDecimal("ANOFABRIC");

        if (anoFabric != null && anoFabric.compareTo(ANO_MINIMO) < 0) {
            throw new MGEModelException("Não é permitido cadastrar veículos com ano de fabricação anterior a 2021.");
        }
    }

@Override
public void beforeUpdate(PersistenceEvent arg0) throws Exception {
    DynamicVO vo = (DynamicVO) arg0.getVo();
    BigDecimal anoFabric = vo.asBigDecimal("ANOFABRIC");

    // Só avisa se o ano for anterior a 2021
    if (anoFabric != null && anoFabric.compareTo(ANO_MINIMO) < 0) {
        // Adiciona uma mensagem de aviso (aparece em amarelo no topo da tela)
    	throw new MGEModelException(
            "Atenção: O veículo possui ano de fabricação anterior a 2021. "
            + "Verifique se isso está correto antes de prosseguir."
        );
        // Não lança exceção → permite salvar
    }
}
}