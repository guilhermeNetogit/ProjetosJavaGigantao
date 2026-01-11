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

public class AlteraValor implements AcaoRotinaJava {
   public void doAction(ContextoAcao ctx) throws Exception {// Eclipse -> Github @guilhermeNetogit passou aqui em 11/01/2026 16:20:25
      new BigDecimal(0);
      new BigDecimal(0);
      String tipMov = "";
      new BigDecimal(0);
      new BigDecimal(0);
      Date referencia = null;
      Double vlrMovNew = (Double)ctx.getParam("VLRMOV");

      for(int i = 0; i < ctx.getLinhas().length; ++i) {
         Registro line = ctx.getLinhas()[i];
         BigDecimal sequencia = (BigDecimal)line.getCampo("SEQUENCIA");
         BigDecimal codEvento = (BigDecimal)line.getCampo("CODEVENTO");
         tipMov = (String)line.getCampo("TIPMOV");
         BigDecimal codFunc = (BigDecimal)line.getCampo("CODFUNC");
         BigDecimal codEmp = (BigDecimal)line.getCampo("CODEMP");
         referencia = (Date)line.getCampo("REFERENCIA");
         this.alterarVlrMov(vlrMovNew, sequencia, codEvento, tipMov, codFunc, codEmp, referencia);
      }

      ctx.setMensagemRetorno("Sucesso! by Guilherme");
   }

   public void alterarVlrMov(Double vlrMovNew, BigDecimal sequencia, BigDecimal codEvento, String tipMov, BigDecimal codFunc, BigDecimal codEmp, Date referencia) {
      EntityFacade entityFacade = EntityFacadeFactory.getDWFFacade();
      JdbcWrapper jdbc = entityFacade.getJdbcWrapper();
      NativeSql nativeSql = new NativeSql(jdbc);

      try {
         nativeSql.executeUpdate("UPDATE TFPMOV SET VLRMOV = " + vlrMovNew
        		 				+ " WHERE REFERENCIA = CONVERT(datetime, '" + referencia + "', 120) " 
        		 				+ " AND CODEMP = " + codEmp 
        		 				+ " AND CODFUNC = " + codFunc 
        		 				+ " AND TIPMOV = '" + tipMov + "'" 
        		 				+ " AND CODEVENTO =" + codEvento 
        		 				+ " AND SEQUENCIA =" + sequencia);
      } catch (Exception var15) {
         throw new RuntimeException(var15);
      } finally {
         jdbc.closeSession();
         JdbcWrapper.closeSession(jdbc);
         NativeSql.releaseResources(nativeSql);
      }

   }
}