package com.maven.repairshop.ui.dialogs;

import com.maven.repairshop.model.Appareil;
import com.maven.repairshop.model.Client;
import com.maven.repairshop.model.Reparation;
import com.maven.repairshop.model.Reparateur;
import com.maven.repairshop.service.ClientService;
import com.maven.repairshop.service.exceptions.BusinessException;
import com.maven.repairshop.ui.controllers.ReparationController;
import com.maven.repairshop.ui.session.SessionContext;
import com.maven.repairshop.ui.util.ServiceRegistry;
import com.maven.repairshop.ui.util.UiDialogs;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class ReparationDialog extends JDialog {

    private final SessionContext session;
    private final ReparationController reparationCtrl = new ReparationController();
    private final ClientService clientService = ServiceRegistry.get().clients();

    private boolean saved = false;
    private Reparation created;

    private JComboBox<Client> cbClient;
    private JTextArea txtCommentaireClient;
    private JTextField txtAvance;

    private JTable tableAppareils;
    private DefaultTableModel appareilsModel;

    private JTextField txtImei;
    private JTextField txtType;
    private JTextField txtDesc;

    private JButton btnSave;

    public ReparationDialog(Window owner, SessionContext session) {
        super(owner, "Nouvelle réparation", ModalityType.APPLICATION_MODAL);
        this.session = session;

        setSize(760, 560);
        setLocationRelativeTo(owner);
        setContentPane(buildUi());
        loadClients();
    }

    public boolean isSaved() { return saved; }
    public Reparation getCreated() { return created; }

    private JComponent buildUi() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBorder(new EmptyBorder(14, 14, 14, 14));

        JLabel title = new JLabel("Créer une réparation");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 18f));
        root.add(title, BorderLayout.NORTH);

        JPanel body = new JPanel(new BorderLayout(12, 12));
        body.setBorder(new EmptyBorder(12, 0, 12, 0));

        body.add(buildLeftForm(), BorderLayout.WEST);
        body.add(buildAppareils(), BorderLayout.CENTER);

        root.add(body, BorderLayout.CENTER);
        root.add(buildActions(), BorderLayout.SOUTH);

        return root;
    }

    private JComponent buildLeftForm() {
        JPanel left = new JPanel();
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        left.setPreferredSize(new Dimension(310, 0));

        cbClient = new JComboBox<>();
        cbClient.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof Client c) {
                    setText(c.getNom() + (c.getTelephone() != null ? " — " + c.getTelephone() : ""));
                }
                return this;
            }
        });

        txtCommentaireClient = new JTextArea(6, 20);
        txtCommentaireClient.setLineWrap(true);
        txtCommentaireClient.setWrapStyleWord(true);

        txtAvance = new JTextField();

        left.add(labeled("Client", cbClient));
        left.add(Box.createVerticalStrut(10));
        left.add(labeled("Avance (optionnel)", txtAvance));
        left.add(Box.createVerticalStrut(10));

        JScrollPane sp = new JScrollPane(txtCommentaireClient);
        left.add(labeled("Commentaire client (optionnel)", sp));

        return left;
    }

    private JComponent buildAppareils() {
        JPanel center = new JPanel(new BorderLayout(8, 8));

        appareilsModel = new DefaultTableModel(new Object[]{"IMEI", "Type", "Description"}, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };

        tableAppareils = new JTable(appareilsModel);
        tableAppareils.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        center.add(new JScrollPane(tableAppareils), BorderLayout.CENTER);
        center.add(buildAddAppareilBar(), BorderLayout.SOUTH);

        JLabel hint = new JLabel("Ajoute au moins 1 appareil (IMEI unique).");
        hint.setForeground(new Color(90, 90, 90));
        center.add(hint, BorderLayout.NORTH);

        return center;
    }

    private JComponent buildAddAppareilBar() {
        JPanel bar = new JPanel(new BorderLayout(8, 8));

        JPanel fields = new JPanel(new GridLayout(2, 3, 8, 8));
        txtImei = new JTextField();
        txtType = new JTextField();
        txtDesc = new JTextField();

        fields.add(new JLabel("IMEI"));
        fields.add(new JLabel("Type"));
        fields.add(new JLabel("Description"));
        fields.add(txtImei);
        fields.add(txtType);
        fields.add(txtDesc);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        JButton btnAdd = new JButton("Ajouter");
        JButton btnRemove = new JButton("Supprimer");
        actions.add(btnRemove);
        actions.add(btnAdd);

        btnAdd.addActionListener(e -> addAppareilRow());
        btnRemove.addActionListener(e -> removeSelectedAppareil());

        bar.add(fields, BorderLayout.CENTER);
        bar.add(actions, BorderLayout.EAST);

        return bar;
    }

    private JComponent buildActions() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));

        JButton btnCancel = new JButton("Annuler");
        btnSave = new JButton("Enregistrer");

        btnCancel.addActionListener(e -> dispose());
        btnSave.addActionListener(e -> save());

        p.add(btnCancel);
        p.add(btnSave);
        return p;
    }

    private JPanel labeled(String label, JComponent field) {
        JPanel p = new JPanel(new BorderLayout(0, 6));
        JLabel l = new JLabel(label);
        l.setFont(l.getFont().deriveFont(Font.BOLD));
        p.add(l, BorderLayout.NORTH);
        p.add(field, BorderLayout.CENTER);
        return p;
    }

    private void loadClients() {
        try {
            Long userId = session.getCurrentUser().getId();
            Long reparateurId = (session.getCurrentUser() instanceof Reparateur) ? userId : null;

            List<Client> clients = clientService.rechercher("", reparateurId, userId);

            DefaultComboBoxModel<Client> m = new DefaultComboBoxModel<>();
            for (Client c : clients) m.addElement(c);
            cbClient.setModel(m);

        } catch (Exception ex) {
            UiDialogs.error(this, "Impossible de charger les clients : " + ex.getMessage());
        }
    }

    private void addAppareilRow() {
        String imei = txtImei.getText().trim();
        String type = txtType.getText().trim();
        String desc = txtDesc.getText().trim();

        if (imei.isEmpty() || type.isEmpty()) {
            UiDialogs.error(this, "IMEI et Type sont obligatoires.");
            return;
        }

        for (int i = 0; i < appareilsModel.getRowCount(); i++) {
            if (imei.equalsIgnoreCase(String.valueOf(appareilsModel.getValueAt(i, 0)))) {
                UiDialogs.error(this, "Cet IMEI est déjà ajouté.");
                return;
            }
        }

        appareilsModel.addRow(new Object[]{imei, type, desc});
        txtImei.setText("");
        txtType.setText("");
        txtDesc.setText("");
        txtImei.requestFocusInWindow();
    }

    private void removeSelectedAppareil() {
        int row = tableAppareils.getSelectedRow();
        if (row >= 0) appareilsModel.removeRow(row);
    }

    private void save() {
        btnSave.setEnabled(false);
        try {
            Client client = (Client) cbClient.getSelectedItem();
            if (client == null) {
                UiDialogs.error(this, "Veuillez sélectionner un client.");
                return;
            }

            if (appareilsModel.getRowCount() == 0) {
                UiDialogs.error(this, "Ajoutez au moins un appareil.");
                return;
            }

            Double avance = null;
            String avanceTxt = txtAvance.getText().trim();
            if (!avanceTxt.isEmpty()) {
                try {
                    avance = Double.parseDouble(avanceTxt);
                    if (avance < 0) throw new NumberFormatException();
                } catch (NumberFormatException nfe) {
                    UiDialogs.error(this, "Avance invalide (nombre >= 0).");
                    return;
                }
            }

            List<Appareil> appareils = new ArrayList<>();
            for (int i = 0; i < appareilsModel.getRowCount(); i++) {
                Appareil a = new Appareil();
                a.setImei(String.valueOf(appareilsModel.getValueAt(i, 0)));
                a.setTypeAppareil(String.valueOf(appareilsModel.getValueAt(i, 1)));
                a.setDescription(String.valueOf(appareilsModel.getValueAt(i, 2)));
                appareils.add(a);
            }

            String commentaire = txtCommentaireClient.getText().trim();
            if (commentaire.isEmpty()) commentaire = null;

            Long userId = session.getCurrentUser().getId();
            Long reparateurId = (session.getCurrentUser() instanceof Reparateur)
                    ? userId
                    : (client.getReparateur() != null ? client.getReparateur().getId() : null);

            if (reparateurId == null) {
                UiDialogs.error(this, "Impossible de déterminer le réparateur pour ce client.");
                return;
            }

            //
            created = reparationCtrl.creerReparation(
                    client.getId(),
                    reparateurId,
                    appareils,
                    commentaire,
                    avance,
                    userId
            );

            saved = true;
            UiDialogs.success(this, "Réparation créée : " + created.getCodeUnique());
            dispose();

        } catch (BusinessException ex) {
            UiDialogs.error(this, ex.getMessage());
        } catch (Exception ex) {
            UiDialogs.error(this, "Erreur inattendue : " + ex.getMessage());
        } finally {
            btnSave.setEnabled(true);
        }
    }
}