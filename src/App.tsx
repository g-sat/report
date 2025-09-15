import { useState, useEffect } from 'react'
import './App.css'

interface Item {
  id: number;
  name: string;
  quantity: number;
  price: number;
  total: number;
}

type ReportFormat = 'pdf' | 'xlsx' | 'csv' | 'docx' | 'pptx' | 'html';

function App() {
  const [items, setItems] = useState<Item[]>([]);
  const [loading, setLoading] = useState(true);
  const [newItem, setNewItem] = useState({ name: '', quantity: 0, price: 0 });

  // Fetch items from backend
  const fetchItems = async () => {
    try {
      const response = await fetch('/api/items');
      if (response.ok) {
        const data = await response.json();
        setItems(data);
      } else {
        console.error('Failed to fetch items');
      }
    } catch (error) {
      console.error('Error fetching items:', error);
    } finally {
      setLoading(false);
    }
  };

  const [showPreview, setShowPreview] = useState(false);
  const [pdfUrl, setPdfUrl] = useState<string | null>(null);
  const [format, setFormat] = useState<ReportFormat>('pdf');

  const handlePreview = async () => {
    const res = await fetch(`/api/reports/preview?format=${format}`);
    const blob = await res.blob();
    setPdfUrl(URL.createObjectURL(blob));
    setShowPreview(true);
  };

  const handlePreviewInNewTab = async () => {
    const res = await fetch(`/api/reports/preview?format=${format}`);
    const blob = await res.blob();
    const url = URL.createObjectURL(blob);
    window.open(url, '_blank', 'noopener,noreferrer');
  };

  useEffect(() => {
    fetchItems();
  }, []);

  const addItem = async () => {
    if (newItem.name && newItem.quantity > 0 && newItem.price > 0) {
      try {
        const response = await fetch('/api/items', {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
          },
          body: JSON.stringify({
            name: newItem.name,
            quantity: newItem.quantity,
            price: newItem.price
          })
        });
        
        if (response.ok) {
          await fetchItems(); // Refresh the list
          setNewItem({ name: '', quantity: 0, price: 0 });
        } else {
          alert('Failed to add item');
        }
      } catch (error) {
        console.error('Error adding item:', error);
        alert('Error adding item');
      }
    }
  };

  const removeItem = async (id: number) => {
    try {
      const response = await fetch(`/api/items/${id}`, {
        method: 'DELETE'
      });
      
      if (response.ok) {
        await fetchItems(); // Refresh the list
      } else {
        alert('Failed to remove item');
      }
    } catch (error) {
      console.error('Error removing item:', error);
      alert('Error removing item');
    }
  };

  const getGrandTotal = () => {
    return items.reduce((sum, item) => sum + (item.quantity * item.price), 0);
  };

  const generateReport = async () => {
    try {
      const res = await fetch(`/api/reports/generate?format=${format}`);
      if (!res.ok) throw new Error('Failed to generate report');
      const blob = await res.blob();
      const url = window.URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      a.download = `inventory-report.${format === 'pdf' ? 'pdf' : format}`;
      document.body.appendChild(a);
      a.click();
      a.remove();
      window.URL.revokeObjectURL(url);
    } catch (err) {
      console.error(err);
      alert('Could not download report. Is the backend running on port 8080?');
    }
  };

  if (loading) {
    return <div className="app"><h2>Loading...</h2></div>;
  }

  return (
    <div className="app">
  <h1 style={{letterSpacing: '1px', fontWeight: 700, color: '#1976d2', marginBottom: 10, fontSize: '2.5rem'}}>Sales Inventory Management</h1>
  <p style={{textAlign: 'center', color: '#666', marginBottom: 30, fontSize: '1.1rem'}}>Professional inventory, PDF reporting, and real-time updates</p>
      
      {/* Add New Item Form */}
      <div className="add-item-form">
        <h3>Add New Item</h3>
        <div className="form-row">
          <input
            type="text"
            placeholder="Item name"
            value={newItem.name}
            onChange={(e) => setNewItem({...newItem, name: e.target.value})}
            style={{minWidth: 160}}
          />
          <input
            type="number"
            placeholder="Quantity"
            value={newItem.quantity || ''}
            onChange={(e) => setNewItem({...newItem, quantity: parseInt(e.target.value) || 0})}
            min={1}
            style={{minWidth: 100}}
          />
          <input
            type="number"
            step="0.01"
            placeholder="Price"
            value={newItem.price || ''}
            onChange={(e) => setNewItem({...newItem, price: parseFloat(e.target.value) || 0})}
            min={0.01}
            style={{minWidth: 100}}
          />
          <button onClick={addItem} style={{fontWeight: 600}}>Add Item</button>
        </div>
      </div>

      {/* Data Table */}
      <div className="table-container">
        <h3>Inventory Items</h3>
        <table className="data-table">
          <thead>
            <tr>
              <th>ID</th>
              <th>Item Name</th>
              <th>Quantity</th>
              <th>Price ($)</th>
              <th>Total ($)</th>
              <th>Actions</th>
            </tr>
          </thead>
          <tbody>
            {items.length === 0 ? (
              <tr>
                <td colSpan={6} style={{textAlign: 'center', color: '#888'}}>No items in inventory.</td>
              </tr>
            ) : (
              items.map((item) => (
                <tr key={item.id}>
                  <td>{item.id}</td>
                  <td>{item.name}</td>
                  <td>{item.quantity}</td>
                  <td>${item.price.toFixed(2)}</td>
                  <td>${(item.quantity * item.price).toFixed(2)}</td>
                  <td>
                    <button 
                      className="remove-btn"
                      onClick={() => removeItem(item.id)}
                    >
                      Remove
                    </button>
                  </td>
                </tr>
              ))
            )}
          </tbody>
          <tfoot>
            <tr>
              <td colSpan={4}><strong>Grand Total:</strong></td>
              <td><strong>${getGrandTotal().toFixed(2)}</strong></td>
              <td></td>
            </tr>
          </tfoot>
        </table>
      </div>

      

      {/* Report Generation */}
      <div className="report-section">
        <h3>Generate & Preview Report</h3>
        <p>Generate a professional PDF report from your inventory data, or preview it before download.</p>
        <div className="report-btn-row" style={{gap: 8, alignItems: 'center', display: 'flex', flexWrap: 'wrap'}}>
          <label style={{fontWeight: 600}}>Format:</label>
          <select value={format} onChange={(e) => setFormat(e.target.value as ReportFormat)}>
            <option value="pdf">PDF</option>
            <option value="xlsx">Excel (.xlsx)</option>
            <option value="csv">CSV</option>
            <option value="docx">Word (.docx)</option>
            <option value="pptx">PowerPoint (.pptx)</option>
            <option value="html">HTML</option>
          </select>
          <button className="generate-report-btn" onClick={generateReport}>
            📥 Download Report
          </button>
          <button className="preview-btn" onClick={handlePreview}>
            👁️ Preview in Popup
          </button>
          <button className="preview-btn" style={{background: '#1976d2', color: '#fff'}} onClick={handlePreviewInNewTab}>
            ↗️ Preview in New Tab
          </button>
        </div>
        {showPreview && pdfUrl && (
          <div className="modal-overlay" onClick={() => setShowPreview(false)}>
            <div className="modal-popup" onClick={e => e.stopPropagation()}>
              <button className="modal-close-btn" onClick={() => setShowPreview(false)}>&times;</button>
              <iframe src={pdfUrl} width="100%" height="600px" title="Report Preview" style={{border: 'none', borderRadius: 12, boxShadow: '0 2px 16px rgba(0,0,0,0.12)'}} />
            </div>
          </div>
        )}
      </div>
    </div>
  )
}

export default App
