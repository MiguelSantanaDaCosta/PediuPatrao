package com.umc.pediupatrao.config;

import com.umc.pediupatrao.entity.enums.EventoPedido;
import com.umc.pediupatrao.entity.enums.StatusPedido;
import org.springframework.context.annotation.Configuration;
import org.springframework.statemachine.config.EnableStateMachineFactory;
import org.springframework.statemachine.config.EnumStateMachineConfigurerAdapter;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;

import java.util.EnumSet;

import static com.umc.pediupatrao.entity.enums.StatusPedido.*;

@Configuration
@EnableStateMachineFactory
public class StateMachineConfig
        extends EnumStateMachineConfigurerAdapter<StatusPedido, EventoPedido> {

    @Override
    public void configure(StateMachineStateConfigurer<StatusPedido, EventoPedido> states)
            throws Exception {
        states.withStates()
              .initial(RECEBIDO)
              .states(EnumSet.allOf(StatusPedido.class))
              .end(FINALIZADO)
              .end(CANCELADO);
    }

    @Override
    public void configure(StateMachineTransitionConfigurer<StatusPedido, EventoPedido> t)
            throws Exception {
        t.withExternal().source(RECEBIDO).target(EM_PREPARACAO).event(EventoPedido.INICIAR_PREPARO)
         .and().withExternal().source(RECEBIDO).target(CANCELADO).event(EventoPedido.CANCELAR)

         .and().withExternal().source(EM_PREPARACAO).target(PRONTO).event(EventoPedido.MARCAR_PRONTO)
         .and().withExternal().source(EM_PREPARACAO).target(CANCELADO).event(EventoPedido.CANCELAR)

         .and().withExternal().source(PRONTO).target(SAIU_PARA_ENTREGA).event(EventoPedido.DESPACHAR)
         .and().withExternal().source(PRONTO).target(RETIRADO).event(EventoPedido.CONFIRMAR_RETIRADA)
         .and().withExternal().source(PRONTO).target(CANCELADO).event(EventoPedido.CANCELAR)

         .and().withExternal().source(SAIU_PARA_ENTREGA).target(FINALIZADO).event(EventoPedido.FINALIZAR)
         .and().withExternal().source(RETIRADO).target(FINALIZADO).event(EventoPedido.FINALIZAR);
    }
}
