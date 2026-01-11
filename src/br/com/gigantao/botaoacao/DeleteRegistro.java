package br.com.gigantao.botaoacao;

import br.com.sankhya.extensions.actionbutton.AcaoRotinaJava;
import br.com.sankhya.extensions.actionbutton.ContextoAcao;
import br.com.sankhya.extensions.actionbutton.Registro;
import br.com.sankhya.jape.EntityFacade;
import br.com.sankhya.jape.dao.JdbcWrapper;
import br.com.sankhya.jape.sql.NativeSql;
import br.com.sankhya.modelcore.util.EntityFacadeFactory;
import java.math.BigDecimal;
import java.util.Date;

public class DeleteRegistro implements AcaoRotinaJava {// Eclipse -> Github @guilhermeNetogit passou aqui em 11/01/2026 20:10:40
   public void doAction(ContextoAcao ctx) throws Exception {

      String tipMov = "";
      Date referencia = null;
      boolean t = ctx.confirmarSimNao("Excluir movimento(s)", "<b>Atenção!!!</b><br><br>"
    		  							+ "Tem certeza que deseja excluir " + ctx.getLinhas().length + " movimento(s)?<br>"
    		  							+ "A ação não poderá ser desfeita.", 1);
      if (t) {
         for(int i = 0; i < ctx.getLinhas().length; ++i) {
            Registro line = ctx.getLinhas()[i];
            BigDecimal sequencia = (BigDecimal)line.getCampo("SEQUENCIA");
            BigDecimal codEvento = (BigDecimal)line.getCampo("CODEVENTO");
            tipMov = (String)line.getCampo("TIPMOV");
            BigDecimal codFunc = (BigDecimal)line.getCampo("CODFUNC");
            BigDecimal codEmp = (BigDecimal)line.getCampo("CODEMP");
            referencia = (Date)line.getCampo("REFERENCIA");
            this.removerRegistro(ctx, sequencia, codEvento, tipMov, codFunc, codEmp, referencia);
         }

         ctx.setMensagemRetorno("Registro(s) deletado(s) com sucesso! <br><br> by Guilherme");
      }

   }

   public void removerRegistro(ContextoAcao ctx, BigDecimal sequencia, BigDecimal codEvento, String tipMov, BigDecimal codFunc, BigDecimal codEmp, Date referencia) {
      EntityFacade entityFacade = EntityFacadeFactory.getDWFFacade();
      JdbcWrapper jdbc = entityFacade.getJdbcWrapper();
      NativeSql nativeSql = new NativeSql(jdbc);

      try {
    	  nativeSql.setNamedParameter("REFERENCIA", referencia);
    	  nativeSql.setNamedParameter("CODEMP", codEmp);
    	  nativeSql.setNamedParameter("CODFUNC", codFunc);
    	  nativeSql.setNamedParameter("TIPMOV", tipMov);
    	  nativeSql.setNamedParameter("CODEVENTO", codEvento);
    	  nativeSql.setNamedParameter("SEQUENCIA", sequencia);
    	  
    	  nativeSql.executeUpdate("DELETE TFPMOV"
        		 				+ " WHERE REFERENCIA = CONVERT(datetime, '" + referencia + "', 120) "
        		 				+ " AND CODEMP		 = " + codEmp 
        		 				+ " AND CODFUNC		 = " + codFunc 
        		 				+ " AND TIPMOV 		 = '" + tipMov + "'" 
        		 				+ " AND CODEVENTO	 =" + codEvento 
        		 				+ " AND SEQUENCIA	 =" + sequencia);
      } catch (Exception var15) {
         throw new RuntimeException(var15);
      } finally {
         jdbc.closeSession();
         JdbcWrapper.closeSession(jdbc);
         NativeSql.releaseResources(nativeSql);
      }
   }
}
