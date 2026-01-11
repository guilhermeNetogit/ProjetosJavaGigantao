package br.com.sankhya.action.botaoacao;
import br.com.sankhya.extensions.actionbutton.AcaoRotinaJava;
import br.com.sankhya.extensions.actionbutton.ContextoAcao;
import br.com.sankhya.extensions.actionbutton.QueryExecutor;
import br.com.sankhya.extensions.actionbutton.Registro;
import br.com.sankhya.jape.EntityFacade;
import br.com.sankhya.jape.bmp.PersistentLocalEntity;
import br.com.sankhya.jape.util.JapeSessionContext;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.jape.vo.PrePersistEntityState;
import br.com.sankhya.jape.wrapper.JapeFactory;
import br.com.sankhya.jape.wrapper.JapeWrapper;
import br.com.sankhya.jape.wrapper.fluid.FluidCreateVO;
import br.com.sankhya.modelcore.auth.AuthenticationInfo;
import br.com.sankhya.modelcore.comercial.BarramentoRegra;
import br.com.sankhya.modelcore.comercial.CentralFaturamento;
import br.com.sankhya.modelcore.comercial.ConfirmacaoNotaHelper;
import br.com.sankhya.modelcore.comercial.LiberacaoSolicitada;
import br.com.sankhya.modelcore.comercial.centrais.CACHelper;
import br.com.sankhya.modelcore.util.EntityFacadeFactory;
import br.com.sankhya.modelcore.util.MGECoreParameter;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

public class GeraReqProdAlmox_v1 implements AcaoRotinaJava {// Eclipse -> Github @guilhermeNetogit passou aqui em 11/01/2026 14:42:50
   private String msgRetorno = "";
   private String temDados = "N";

   public void doAction(ContextoAcao ctx) throws Exception {
      System.out.println("GeraMovPortal");
      new StringBuffer();
      StringBuffer sql = new StringBuffer();
      QueryExecutor queryUpd = ctx.getQuery();
      sql.append(" DELETE FROM AD_SOLCOMPRAOS ");

      try {
         queryUpd.update(sql.toString());
      } catch (Exception var6) {
         this.msgRetorno = this.msgRetorno + var6.getMessage();
         var6.printStackTrace();
      }

      for(int i = 0; i < ctx.getLinhas().length; ++i) {
         Registro line = ctx.getLinhas()[i];
         this.gravarDados(line, ctx);
      }

      this.gerarPedido(ctx);
      if (this.temDados == "N") {
         this.msgRetorno = " Os produtos selecionados Não possuem estoque ou ja foram gerados!";
      }

      if (this.msgRetorno.equals("")) {
         this.msgRetorno = "Movimento gerado com sucesso!";
      } else {
         this.msgRetorno = "<b>Falha na geração da Solicitação de Compra: </b><br>" + this.msgRetorno;
      }

      ctx.setMensagemRetorno(this.msgRetorno);
   }

   public void gravarDados(Registro line, ContextoAcao ctx) throws Exception {
      BigDecimal nuOS = (BigDecimal)line.getCampo("NUOS");
      BigDecimal sequencia = (BigDecimal)line.getCampo("SEQUENCIA");
      StringBuffer sql = new StringBuffer();
      QueryExecutor query = ctx.getQuery();
      sql.append(" SELECT ITE.CODPROD, ITE.CODVOL, ISNULL(ITE.CODLOCAL,0) AS CODLOCAL, ISNULL(ITE.CONTROLE,' ') AS CONTROLE, ISNULL(ITE.VLRUNIT,0) AS VLRUNIT, ITE.VLRTOT, ISNULL(ITE.AD_CODGRUPOPROD,0) AS AD_CODGRUPOPROD, ");
      sql.append(" ITE.QTDNEG AS QTD ");
      sql.append("   FROM TCFPRODOS ITE ");
      sql.append(" WHERE ITE.QTDNEG > 0 ");
      sql.append("  AND ITE.AD_NUNOTAREQ IS NULL ");
      sql.append("  AND ITE.NUOS = ").append(nuOS);
      sql.append("  AND ITE.SEQUENCIA = ").append(sequencia);
      sql.append("  AND ITE.QTDNEG <= ISNULL((SELECT SUM(EST.ESTOQUE-EST.RESERVADO) FROM TGFEST EST WHERE EST.CODPROD = ITE.CODPROD AND EST.CODLOCAL = ISNULL(ITE.CODLOCAL,0) AND EST.CONTROLE = ISNULL(ITE.CONTROLE,' ')),0)   ");
      System.out.println(sql.toString());

      try {
         query.nativeSelect(sql.toString());

         while(query.next()) {
            BigDecimal codprod = query.getBigDecimal("CODPROD");
            String codvol = query.getString("CODVOL");
            BigDecimal codlocal = query.getBigDecimal("CODLOCAL");
            String controle = query.getString("CONTROLE");
            BigDecimal qtd = query.getBigDecimal("QTD");
            BigDecimal vlrunit = query.getBigDecimal("VLRUNIT");
            BigDecimal vlrtot = query.getBigDecimal("VLRTOT");
            BigDecimal codgrupoprodprod = query.getBigDecimal("AD_CODGRUPOPROD");
            JapeWrapper empresaDAO = JapeFactory.dao("AD_SOLCOMPRAOS");
            DynamicVO var16 = ((FluidCreateVO)((FluidCreateVO)((FluidCreateVO)((FluidCreateVO)((FluidCreateVO)((FluidCreateVO)((FluidCreateVO)((FluidCreateVO)((FluidCreateVO)((FluidCreateVO)empresaDAO.create().set("NUOS", nuOS)).set("SEQUENCIA", sequencia)).set("CODPROD", codprod)).set("CODVOL", codvol)).set("CODLOCAL", codlocal)).set("CONTROLE", controle)).set("QTD", qtd)).set("VLRUNIT", vlrunit)).set("VLRTOT", vlrtot)).set("CODGRUPOPROD", codgrupoprodprod)).save();
         }
      } catch (Exception var17) {
         this.msgRetorno = this.msgRetorno + var17.getMessage();
         var17.printStackTrace();
      }

   }

   public void gerarPedido(ContextoAcao ctx) throws Exception {
      StringBuffer sql = new StringBuffer();
      QueryExecutor query = ctx.getQuery();
      BigDecimal codTOP = (BigDecimal)MGECoreParameter.getParameter("SERVTOPREQ");
      sql.append(" SELECT DISTINCT ISNULL(S.CODGRUPOPROD,0) AS CODGRUPOPROD, ");
      sql.append(" OS.NUOS, ISNULL(OS.KM,OS.HORIMETRO) AS HORIMETRO, OS.DATAINI, OS.CODEMP, OS.CODVEICULO, OS.CODPARC,  ");
      sql.append("  OS.CODCENCUS, OS.CODNAT, OS.CODPROJ, " + codTOP + " AS CODTIPOPER , 'J' AS TIPMOV, S.NUOS ");
      sql.append("  FROM AD_SOLCOMPRAOS S, TCFOSCAB OS ");
      sql.append("WHERE S.NUOS = OS.NUOS ");
      System.out.println(sql.toString());

      try {
         query.nativeSelect(sql.toString());

         while(query.next()) {
            this.temDados = "S";
            Timestamp pData = query.getTimestamp("DATAINI");
            BigDecimal pCodVeiculo = query.getBigDecimal("CODVEICULO");
            BigDecimal codparc = query.getBigDecimal("CODPARC");
            BigDecimal pCodEmp = query.getBigDecimal("CODEMP");
            BigDecimal pHorimetro = query.getBigDecimal("HORIMETRO");
            BigDecimal pCodCenCus = query.getBigDecimal("CODCENCUS");
            BigDecimal pCodNat = query.getBigDecimal("CODNAT");
            BigDecimal pCodProj = query.getBigDecimal("CODPROJ");
            BigDecimal pCodTipOper = query.getBigDecimal("CODTIPOPER");
            String pTipMov = query.getString("TIPMOV");
            BigDecimal codTipNeg = new BigDecimal(11);
            BigDecimal nuOS = query.getBigDecimal("NUOS");
            BigDecimal codgrupoprod = query.getBigDecimal("CODGRUPOPROD");
            System.out.println("GeraMovPortal - dentro loop primeiro select codparc: " + codparc);
            EntityFacade dwfEntityFacade = EntityFacadeFactory.getDWFFacade();
            DynamicVO cabVO = (DynamicVO)dwfEntityFacade.getDefaultValueObjectInstance("CabecalhoNota");
            cabVO.setProperty("CODTIPOPER", pCodTipOper);
            cabVO.setProperty("TIPMOV", pTipMov);
            cabVO.setProperty("CODPARC", codparc);
            cabVO.setProperty("CODEMP", pCodEmp);
            cabVO.setProperty("DTNEG", pData);
            cabVO.setProperty("CODNAT", pCodNat);
            cabVO.setProperty("CODCENCUS", pCodCenCus);
            cabVO.setProperty("CODPROJ", pCodProj);
            cabVO.setProperty("CODVEICULO", pCodVeiculo);
            cabVO.setProperty("CIF_FOB", "S");
            cabVO.setProperty("CODTIPVENDA", codTipNeg);
            cabVO.setProperty("KMVEICULO", pHorimetro);
            cabVO.setProperty("NUOSCAB", nuOS);
            BigDecimal nuNotaGerado = new BigDecimal(0);

            try {
               CACHelper cacHelper = new CACHelper();
               AuthenticationInfo auth = AuthenticationInfo.getCurrent();
               JapeSessionContext.putProperty("br.com.sankhya.com.CentralCompraVenda", Boolean.TRUE);
               PrePersistEntityState cabPreState = PrePersistEntityState.build(dwfEntityFacade, "CabecalhoNota", cabVO);
               BarramentoRegra bRegrasCab = cacHelper.incluirAlterarCabecalho(auth, cabPreState);
               DynamicVO newCabVO = bRegrasCab.getState().getNewVO();
               nuNotaGerado = newCabVO.asBigDecimal("NUNOTA");
               System.out.println("GeraMovPortal - inserindo a nota: " + nuNotaGerado);
               QueryExecutor queryIte = ctx.getQuery();
               sql = new StringBuffer();
               sql.append(" SELECT NUOS, SEQUENCIA, CODPROD, CODVOL, CODLOCAL, CONTROLE, QTD, VLRUNIT, VLRTOT ");
               sql.append("   FROM AD_SOLCOMPRAOS ");
               sql.append("  WHERE ISNULL(CODGRUPOPROD,0)  = ").append(codgrupoprod);
               System.out.println(sql.toString());
               queryIte.nativeSelect(sql.toString());

               // CORREÇÃO 1: Usar abordagem alternativa para incluir itens sem ServiceContext
               JapeWrapper itemNotaDAO = JapeFactory.dao("ItemNota");
               while(queryIte.next()) {
                  System.out.println("Inicio inclusão Produto " + queryIte.getBigDecimal("CODPROD"));
                  
                  // Abordagem alternativa usando JapeWrapper diretamente
                  itemNotaDAO.create()
                      .set("NUNOTA", nuNotaGerado)
                      .set("CODPROD", queryIte.getBigDecimal("CODPROD"))
                      .set("QTDNEG", queryIte.getBigDecimal("QTD"))
                      .set("VLRUNIT", queryIte.getBigDecimal("VLRUNIT"))
                      .set("VLRTOT", queryIte.getBigDecimal("VLRTOT"))
                      .set("PERCDESC", BigDecimal.ZERO)
                      .set("VLRDESC", BigDecimal.ZERO)
                      .set("CODVOL", queryIte.getString("CODVOL"))
                      .set("CODLOCALORIG", queryIte.getBigDecimal("CODLOCAL"))
                      .set("CONTROLE", queryIte.getString("CONTROLE"))
                      .save();
                  
                  System.out.println("Fim inclusão " + queryIte.getBigDecimal("CODPROD"));
               }

               BarramentoRegra barramento = BarramentoRegra.build(CentralFaturamento.class, "regrasConfirmacaoSilenciosa.xml", AuthenticationInfo.getCurrent());
               PrePersistEntityState persistentConf = ConfirmacaoNotaHelper.confirmarNota(nuNotaGerado, barramento);
               DynamicVO notaVO = persistentConf.getNewVO();
               if (!"L".equals(notaVO.asString("STATUSNOTA"))) {
                  StringBuffer erros = new StringBuffer();
                  Exception e;
                  if (!barramento.getErros().isEmpty()) {
                     for(Iterator var33 = barramento.getErros().iterator(); var33.hasNext(); erros.append(e.getMessage())) {
                        e = (Exception)var33.next();
                        if (erros.toString().length() > 0) {
                           erros.append("\n");
                        }
                     }
                  }

                  Collection<LiberacaoSolicitada> liberacoes = barramento.getLiberacoesSolicitadas();
                  if (liberacoes.size() > 0) {
                     String descTipo = "";
                     descTipo = "Solicitação de Compras";
                     this.msgRetorno = this.msgRetorno + " - Nota " + nuNotaGerado + ", do tipo " + descTipo + " necessidade de liberação para confirmação; <br>";
                  }

                  if (erros.toString().length() > 0) {
                     throw new Exception("Mensagem ao tentar confirmar a nota: " + erros.toString());
                  }
               }

               QueryExecutor queryUpd = ctx.getQuery();
               sql = new StringBuffer();
               sql.append(" UPDATE TCFPRODOS ");
               sql.append(" SET AD_NUNOTAREQ = ").append(nuNotaGerado);
               sql.append(" WHERE SEQUENCIA IN (SELECT SEQUENCIA FROM AD_SOLCOMPRAOS WHERE CODGRUPOPROD = ").append(codgrupoprod).append(" )");
               queryUpd.update(sql.toString());
               new StringBuffer();
            } catch (Exception var38) {
               System.out.println("GeraMovPortal: erro NUNOTA: " + nuNotaGerado);
               if (nuNotaGerado.compareTo(new BigDecimal(0)) > 0) {
                  // CORREÇÃO 2: Usar abordagem alternativa para remover a nota
                  try {
                     JapeWrapper cabecalhoNotaDAO = JapeFactory.dao("CabecalhoNota");
                     cabecalhoNotaDAO.delete(nuNotaGerado);
                  } catch (Exception ex) {
                     System.out.println("Erro ao excluir nota: " + ex.getMessage());
                  }
               }

               this.msgRetorno = this.msgRetorno + var38.getMessage();
               var38.printStackTrace();
            }
         }
      } catch (Exception var39) {
         this.msgRetorno = this.msgRetorno + var39.getMessage();
         var39.printStackTrace();
      } finally {
         query.close();
      }

   }
}