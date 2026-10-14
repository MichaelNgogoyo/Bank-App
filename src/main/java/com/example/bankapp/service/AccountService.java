package com.example.bankapp.service;

import com.example.bankapp.model.Account;
import com.example.bankapp.model.Transaction;
import com.example.bankapp.repository.AccountRepository;
import com.example.bankapp.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

@Service
public class AccountService implements UserDetailsService {

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private TransactionRepository transactionRepository;


    public Account findAccountByUsername(String username){
        return accountRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Account Not found"));
    }

    public Account registerAccount(String username, String password){
        if (accountRepository.findByUsername(username).isPresent() ){
            throw new RuntimeException("Username already exists");
        }

        Account account = new Account();
        account.setUsername(username);
        account.setPassword(passwordEncoder.encode(password));
        account.setBalance(BigDecimal.ZERO);

        return accountRepository.save(account);

    }

    public void deposit(Account account, BigDecimal amount){
      account.setBalance(account.getBalance().add( amount));

      accountRepository.save(account);
      Transaction transaction = new Transaction();
      transaction.setAmount(amount);
      transaction.setType("Deposit");
      transaction.setTimeStamp(LocalDateTime.now());
      transaction.setAccount(account);

      transactionRepository.save(transaction);

    }

    public void withdraw(Account account, BigDecimal amount){

        if (account.getBalance().compareTo(amount) < 0){
            throw new RuntimeException("Insufficient balance");
        }

        account.setBalance(account.getBalance().subtract(amount));

        accountRepository.save(account);

        Transaction transaction = new Transaction();
        transaction.setAccount(account);
        transaction.setType("Withdraw");
        transaction.setTimeStamp(LocalDateTime.now());
        transaction.setAmount(amount);

        transactionRepository.save(transaction);
    }


    public List<Transaction> getTransactionHistory(Account account){
        return transactionRepository.findByAccountId(account.getId());
    }

    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException{
        Account account = findAccountByUsername(username);

        if (account == null){
            throw new UsernameNotFoundException("username or password not found");
        }

        return new Account(
                account.getId(),
                account.getUsername(),
                account.getPassword(),
                account.getBalance(),
                account.getTransactions(),
                authorities()
        );
    }

    public Collection<? extends GrantedAuthority> authorities(){
        return Arrays.asList(new SimpleGrantedAuthority("User"));
    }

    public void transferAmount(Account fromAccount, String toUsername, BigDecimal amount){
        if(fromAccount.getBalance().compareTo(amount) < 0){
            throw new RuntimeException("Insufficient funds");
        }

        Account toAccount = accountRepository.findByUsername(toUsername)
                .orElseThrow(() -> new RuntimeException("Recipient account not found"));

        //deduct
        fromAccount.setBalance(fromAccount.getBalance().subtract(amount));
        accountRepository.save(fromAccount);

        //add
        toAccount.setBalance(toAccount.getBalance().add(amount));
        accountRepository.save(toAccount);

        //create transaction records

        Transaction debitTransaction = new Transaction();
        debitTransaction.setAmount(amount);
        debitTransaction.setType("Transfer Out to "+toAccount.getUsername());
        debitTransaction.setTimeStamp(LocalDateTime.now());
        debitTransaction.setAccount(toAccount);

        transactionRepository.save(debitTransaction);

        Transaction creditTransaction = new Transaction();
        creditTransaction.setAmount(amount);
        creditTransaction.setType("Transfer in to "+fromAccount.getUsername());
        creditTransaction.setTimeStamp(LocalDateTime.now());
        creditTransaction.setAccount(fromAccount);

        transactionRepository.save(creditTransaction);
    }

}
