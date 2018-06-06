# OnlineSocialNetwork
Questo è il mio progetto per il corso di laboratorio di reti.

# AVVISO IMPORTANTE/CONSIGLIO DA SEGUIRE PER GLI STUDENTI DI INFORMATICA CHE DEVONO SOSTENERE ANCORA L'ESAME
La professoressa conosce il mio progetto: consultare il codice può andar bene, copiare sicuramente non vi fa fare bella figura in quanto prevede la bocciatura.

# Progetto
Il progetto consiste nello sviluppo di una rete sociale caratterizzata da un semplice
insieme di funzionalità. Per utilizzare queste funzionalità, gli utenti si devono registrare,
quindi possono: effettuare il login, ricercare un utente specificandone il nome, stabilire amicizie con altri utenti, pubblicare contenuti (testo), ricevere contenuti pubblicati dai loro amici, se hanno manifestato interesse in tali contenuti, effettuare logout

La rete sociale deve essere implementata mediante 2 componenti principali, che
interagiscono usando diversi protocolli e paradigmi di comunicazione di rete ( TCP,
UDP e RMI). Ogni componente sarà composta di un insieme di classi definite in fase di
design del programma. Abbiamo la componente SocialServer che gestisce  la fase di registrazione, memorizza tutti gli utenti registrati e le loro reti di amicizie, crea nuove amicizie quando richiesto dagli utenti, gestisce i contenuti prodotti dagli utenti, verifica la presenza degli utenti online. Inoltre ci sono vari SocialClient che gestiscono l’interazione con l’utente, tramite una user interface, comunicano con il SocialServer per eseguire le azioni richieste dall’utente, inoltre partecipano ad un gruppo di multicast per la gestione dei messaggi di keep­alive.
