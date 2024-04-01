package org.oleg_w570.marksman_game;

import jakarta.persistence.*;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "players")
public class PlayerInfo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    public String nickname;

    public int wins = 0;

    @Transient
    public String color;
    @Transient
    public ArrowInfo arrow = new ArrowInfo();
    @Transient
    public int shots = 0;
    @Transient
    public int score = 0;
    @Transient
    public boolean wantToPause = false;
    @Transient
    public boolean wantToStart = false;
    @Transient
    public boolean shooting = false;

    public PlayerInfo() {
    }

    public PlayerInfo(String nickname) {
        this.nickname = nickname;
    }

    public String getNickname() {
        return nickname;
    }

    public int getWins() {
        return wins;
    }

    public static PlayerInfo loadOrCreateByName(String nickname) {
        try (Session session = SessionFactoryBuilder.getSessionFactory().openSession()) {
            Transaction transaction = session.beginTransaction();
            PlayerInfo player = session.createQuery("FROM PlayerInfo WHERE nickname = :nickname", PlayerInfo.class).setParameter("nickname", nickname).uniqueResult();
            if (player == null) {
                player = new PlayerInfo(nickname);
                session.persist(player);
            }
            transaction.commit();
            return player;
        } catch (Exception e) {
            System.out.println("Load or create player exception: " + e.getMessage());
        }
        return new PlayerInfo(nickname);
    }

    public void increaseWins() {
        ++wins;
        try (Session session = SessionFactoryBuilder.getSessionFactory().openSession()) {
            Transaction transaction = session.beginTransaction();
            session.merge(this);
            transaction.commit();
        } catch (Exception e) {
            System.out.println("Increase wins exception " + e.getMessage());
        }
    }

    public static List<PlayerInfo> getAllPlayers() {
        try (Session session = SessionFactoryBuilder.getSessionFactory().openSession()) {
            Transaction transaction = session.beginTransaction();
            List<PlayerInfo> allPlayers = session.createQuery("FROM PlayerInfo ", PlayerInfo.class).getResultList();
            transaction.commit();
            return allPlayers;
        } catch (Exception e) {
            System.out.println("Get all players exception " + e.getMessage());
        }
        return new ArrayList<>();
    }
}
