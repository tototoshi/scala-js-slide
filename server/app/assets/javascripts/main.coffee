App = {}

App.mediator = _.extend({}, Backbone.Events)

App.NoteModel = Backbone.Model.extend
    urlRoot: '/api/note'

App.NoteCollection = Backbone.Collection.extend
    url: '/api/notes'
    model: App.NoteModel

App.SelectorView = Backbone.View.extend
    initialize: ->
        this.listenTo(this.collection, 'sync', this.render)
        this.listenTo(this.model, 'sync', this.selectCurrent)
        this.listenTo(this.model, 'destroy', this.removeCurrentSelected)
        this.collection.fetch()
    render: ->
        this.$el.empty()
        this.$el.append $('<option>').val(0).text('[Create a new note]')
        this.collection.each (note) =>
            this.$el.append($('<option>').val(note.id).text(note.get('title')))
        this.selectCurrent()
        return this
    selectCurrent: ->
        if this.model.isNew()
            this.$('option[value=0]').attr('selected', 'selected')
        else
            if this.$('option[value=' + this.model.id + ']').length == 0
                this.collection.fetch()
            else
                this.$('option[value=' + this.model.id + ']').attr('selected', 'selected')
    triggerEditorMove: ->
        if this.$el.val() == '0'
            this.model.clear()
        else
            this.model.set('id', this.$el.val(), {silent: true})
            this.model.fetch({wait: true})
    changeTitle: (title) ->
        if this.$el.val() != '0'
            this.$el.find(':selected').text(title)
    removeCurrentSelected: (e) ->
        this.$('option:selected').remove()
        this.$('option[value=0]').attr('selected', 'selected')
    events:
        "change": "triggerEditorMove"

App.DeleteButtonView = Backbone.View.extend
    initialize: ->
        this.render()
        this.listenTo(this.model, 'change', this.render)
    events:
        "click": "delete"
    render: ->
        if this.model.id
            this.$el.html('<input type="button" id="delete" value="delete" class="btn-danger">')
        return this
    delete: ->
        this.model.destroy
            success: (model, response) =>
                App.mediator.trigger('notify-deleted')
                this.model.clear()
            error: (model, response) ->
                App.mediator.trigger('notify-error')

App.SlideStartView = Backbone.View.extend
    initialize: ->
        this.render()
        this.listenTo(this.model, 'change', this.render)
    render: ->
        if this.model.id
            this.$el.html('<input type="button" id="slide-start" value="slide-start" class="btn-primary" data-toggle="modal" data-target="#slide-modal">')
        return this

App.EditorView = Backbone.View.extend
    initialize: ->
        if ! this.model.isNew()
            this.model.fetch()

        this.render()
        this.listenTo(this.model, 'change', this.render)
        this.listenTo(this.model, 'destroy', this.clear)
        this.listenTo(this.model, 'reset', this.reset)
    clear: ->
        this.$el.val('')
        $('#view').html('')
    reset: ->
        this.$el.empty()
    render: ->
        this.$el.val(this.model.get('raw'))
        $.post(
            '/api/render'
            { raw: this.$el.val() }
            (data, textStatus, jqXHR) ->
                $('#view').html(data)
                $('#slide').html(data)
        )
        return this
    updateNote: ->
        raw = this.$el.val()
        title = raw.replace(/^[#\s]*|\s*$/g, "").split("\n")[0]
        App.mediator.trigger('title-change', title)
        this.model.save {title, title, raw: raw},
            success: (model, response) =>
                App.mediator.trigger('notify-saved')
            error: (model, response) ->
                App.mediator.trigger('notify-error')
    debounceUpdateNote:
        _.debounce(
            -> this.updateNote(),
            500
        )
    events:
        "keydown": "debounceUpdateNote"
        "click #delete": "delete"

App.NotifView = Backbone.View.extend
    notifySaved: ->
        notif
            msg: "Saved"
            type: "success"
            position: "right"
            height: 50
            width: 100
    notifyError: ->
        notif
            msg: "Oops!"
            type: "error"
            position: "right"
            height: 50
            width: 100
    notifyDeleted: ->
        notif
            msg: "Deleted"
            type: "success"
            position: "right"
            height: 50
            width: 100


App.AppView = Backbone.View.extend
    initialize: ->
        this.render()
    render: ->
        height = this.$el.height()
        $('#editor').css { height: height - 150 }
        $('#view').css { height: height - 150 }
        return this
    events:
        "resize": "render"
$ ->
    appView = new App.AppView
        el: $(window)

    note = new App.NoteModel

    noteCollection = new App.NoteCollection

    new App.SlideStartView { model: note, el: $('#slide-start')}
    new App.DeleteButtonView { model: note, el: $('#delete') }

    editorView = new App.EditorView
        model: note
        collection: noteCollection
        el: $('textarea')

    selectorView = new App.SelectorView
        el: $('select')
        model: note
        collection: noteCollection

    notifView = new App.NotifView

    App.mediator.on('title-change', _.bind(selectorView.changeTitle, selectorView))
    App.mediator.on('notify-saved', _.bind(notifView.notifySaved, notifView))
    App.mediator.on('notify-error', _.bind(notifView.notifyError, notifView))
    App.mediator.on('notify-deleted', _.bind(notifView.notifyDeleted, notifView))

    Backbone.history.start();

